import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, HostListener, Injectable, OnInit, ViewChild} from '@angular/core';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {BehaviorSubject, forkJoin} from 'rxjs';
import {AngularNeo4jService} from 'angular-neo4j';
import {environment} from '../../environments/environment';
import {CookieService} from 'ngx-cookie-service';
import {Network} from 'vis-network';
import {MatSnackBar, MatSnackBarConfig, MatSnackBarRef, SimpleSnackBar} from '@angular/material/snack-bar';

/**
 * Base Node for Tree item nodes
 */
class BaseTreeItemNode {
  name: string;
  label: string;
  code: string;
  path: string;
  filterType: FilterType;
}

/**
 * Node for Tree item
 */
export class TreeItemNode extends BaseTreeItemNode {
  children: TreeItemNode[];
}

/** Flat  item node with expandable and level information */
export class TreeItemFlatNode extends BaseTreeItemNode {
  level: number;
  expandable: boolean;
}

/**
 * Node for Graph
 */
class GraphItem {
  id: number;
  name: string;
  code: string;
  tooltip;
  children: GraphItem[] = [];
  addedDependency: GraphItem[] = [];
  deletedDependency: GraphItem[] = [];
  type: FilterType;
  isDependency: boolean;

  shouldBeDisplayed(): boolean {
    if (this.addedDependency.length > 1 || this.deletedDependency.length > 1 || this.isDependency) {
      return true;
    }
    return this.children.some(child => child.shouldBeDisplayed());
  }

  /**
   * Searches in children
   *
   * @param id child id
   */
  isInChildrenTransitive(id: number): boolean {
    return this.children.some(value => {
      return value.id === id || value.isInChildrenTransitive(id);
    });
  }

  /**
   * Searches if cluster has a child of this GraphItem
   *
   * @param net Network in which the cluster is
   * @param cluster cluster in which should be searched
   */
  hasChildrenInCluster(net: Network, cluster: string): boolean {
    return net.getNodesInCluster(cluster).some(value => {
      if (value.toString().startsWith('cluster:')) {
        return this.hasChildrenInCluster(net, value.toString());
      }
      return this.isInChildrenTransitive(value as number);
    });
  }
}

class IdGenerator {
  lastId = 0;

  getNextId() {
    return this.lastId++;
  }
}

/** Filter types */
export enum FilterType {
  Project = 'pr',
  Package = 'p',
  Class = 'c',
  Method = 'm',
  Dependency = 'd',
  Undefined = 'u'
}

/** Display options */
export enum DisplayOption {
  Standard = 'Normal',
  CompactMiddlePackages = 'Compact Middle Packages',
  FlattenPackages = 'Flat Packages',
  Graph = 'Graph'
}

const reverseDisplayOption = new Map<string, DisplayOption>();
reverseDisplayOption.set('Normal', DisplayOption.Standard);
reverseDisplayOption.set('Compact Middle Packages', DisplayOption.CompactMiddlePackages);
reverseDisplayOption.set('Flat Packages', DisplayOption.FlattenPackages);

/** Display Label */
export enum Label {
  Project = 'Project',
  Package = 'Package',
  Class = 'Class',
  Method = 'Method',
  AddedDependency = '+ Dependency',
  DeletedDependency = '- Dependency'
}

/**
 * Dependency database, it can build a tree structured Json object.
 * Each node in Json object represents a changelog item.
 */
@Injectable()
export class DependencyTreeDatabase {
  dataChange = new BehaviorSubject<TreeItemNode[]>([]);
  treeData: any[];
  changelogIds: any[];
  projectNames: string[];
  selectedProject: string;
  root = 'root';
  selectedDisplayOption: DisplayOption;

  constructor(private neo4j: AngularNeo4jService) {
    this.initialize();
  }

  get data(): TreeItemNode[] {
    return this.dataChange.value;
  }

  initialize() {
    this.treeData = [];
    this.connectToDatabase();
    this.loadProjectName();
    this.selectedDisplayOption = DisplayOption.CompactMiddlePackages;
  }

  /**
   * Connect to Database
   */
  connectToDatabase() {
    const url = environment.neo4jUrl;
    const username = environment.neo4jUsername;
    const password = environment.neo4jPassword;
    const encrypted = environment.neo4jEncrypted;

    this.neo4j
      .connect(
        url,
        username,
        password,
        encrypted
      )
      .then(driver => {
        if (driver) {
          console.log(`Successfully connected to ${url}`);
        }
      });
  }

  /**
   * Load the Identifier for the Changelogs
   */
  loadChangelogIds(projectName: string) {
    this.selectedProject = projectName;
    const params = {pName: projectName.toString()};
    const queryChangelogId = 'MATCH (n:ProjectInformation{name: {pName}}) return n.versions';
    this.neo4j.run(queryChangelogId, params).then(changelogInformationIds => {
      this.changelogIds = changelogInformationIds.flat(2).slice(1);
    });
  }

  /**
   * Load the changelog data for the selected changelog identifier
   * @param version the changelog identifier
   * @param projectName project name
   */
  async loadChangelogFromDatabase(projectName: string, version: string) {
    const params = {pName: projectName.toString(), version: version.toString()};

    const packageInformation: any[] = [{text: '', code: this.root}];
    const classInformation: any[] = [];
    const methodInformation: any[] = [];
    const dependencyClass: any[] = [];
    const dependencyMethod: any[] = [];

    // Query fetching all nodes with contain changes
    const queryTree = 'match p=(r:ProjectInformation{name: {pName}})<-[:Parent *]-' +
      '(i:Information)-[:MethodDependency|ClassDependency|PackageDependency|ProjectDependency]-(di) ' +
      'where single (r in relationships(p) where any(x in keys(r) where x = "versionInfo.' + version + '"))' +
      ' UNWIND nodes(p) AS x WITH DISTINCT x RETURN x.path, x.name, labels(x) as labels ';

    // Query fetching all dependencies
    const queryDependencies = 'match p=(r:ProjectInformation{name: {pName}})<-[:Parent *]-' +
      '(i:Information)-[:MethodDependency|ClassDependency|PackageDependency|ProjectDependency]->(di)' +
      'where single (r in relationships(p) where any(x in keys(r) where x = "versionInfo.' + version + '")) ' +
      'return di.path as path, di.name as name, labels(di) as diLabels,' +
      ' any(x in relationships(p) where x["versionInfo.' + version + '"]) ' +
      'as Changestatus, i.path as iPath, labels(i) as iLabels, i.name as iName';

    const treeResult = this.neo4j.run(queryTree, params).then(nodes => {
      nodes.forEach(node => {
        const path: string = node[0] as string;
        const name: string = node[1];
        const type = this.resolveType(node[2]);
        const obj: { [k: string]: any } = {};
        obj.text = name;
        obj.path = path;
        obj.code = this.root + '.' + path;
        switch (type) {
          case 1: {
            obj.label = Label.Package;
            obj.filterType = FilterType.Package;
            packageInformation.push(obj);
            break;
          }
          case 2: {
            obj.label = Label.Class;
            obj.filterType = FilterType.Class;
            classInformation.push(obj);
            classInformation.push(this.createMethodsNode(obj.code));
            classInformation.push(this.createAddedDependenciesNode(obj.code));
            classInformation.push(this.createDeletedDependenciesNode(obj.code));
            break;
          }
          case 3: {
            obj.label = Label.Method;
            obj.filterType = FilterType.Method;
            obj.code = obj.code.substr(0, obj.code.length - (name.length + 1)) + '.methods.' + name;
            methodInformation.push(obj);
            methodInformation.push(this.createAddedDependenciesNode(obj.code));
            methodInformation.push(this.createDeletedDependenciesNode(obj.code));
            break;
          }
        }
      });
      const project: { [k: string]: any } = {};
      project.text = projectName.toString();
      project.path = projectName.toString();
      project.code = this.root + '.' + project.path;
      project.label = Label.Project;
      project.filterType = FilterType.Project;
      packageInformation.push(project);
    });

    const dependencyResult = this.neo4j.run(queryDependencies, params).then(nodes => {
      nodes.forEach(node => {
        const path = node[0] as string;
        const name = node[1];
        const type = this.resolveType(node[2]);
        const usedByType = this.resolveType(node[5]);
        const usedByName = node[6];
        const status = (node[3] === true) ? 'added' : 'deleted';
        const usedByPath = node[4] as string;
        const obj: { [k: string]: any } = {};
        obj.text = name;
        obj.path = path;
        if (usedByType === 3) {
          obj.code = this.root + '.' +
            usedByPath.substr(0, usedByPath.length - (usedByName.length + 1)) + '.methods.' + usedByName + '.' + status + '.' + name;
        } else {
          obj.code = this.root + '.' + usedByPath + '.' + status + '.' + name;
        }
        obj.filterType = FilterType.Dependency;
        obj.label = (node[3] === true) ? Label.AddedDependency : Label.DeletedDependency;
        switch (type) {
          case 1: {
            // not displayed
            break;
          }
          case 2: {
            dependencyClass.push(obj);
            const cpy = Object.assign({}, obj);
            cpy.code = this.root + '.' + usedByPath.substr(0, usedByPath.length - (usedByName.length + 1)) + '.' + status + '.' + name;
            if (dependencyClass.find(o => o.code === cpy.code) === undefined) {
              dependencyClass.push(cpy);
            }
            break;
          }
          case 3: {
            dependencyMethod.push(obj);
            break;
          }
        }
      });
    });

    // wait for all query results before building the tree
    await forkJoin(treeResult, dependencyResult).toPromise().then(_ => {
      this.treeData = [...packageInformation, ...classInformation, ...methodInformation, ...dependencyClass, ...dependencyMethod];
      const data = this.buildDependencyTree(this.treeData, this.root, this.selectedDisplayOption);
      this.dataChange.next(data);
    });
  }

  resolveType(typeArr: string[]) {
    for (const l of typeArr) {
      if (l.toLowerCase().indexOf('package') > -1) {
        return 1;
      }
      if (l.toLowerCase().indexOf('class') > -1) {
        return 2;
      }
      if (l.toLowerCase().indexOf('method') > -1) {
        return 3;
      }
    }
  }

  createAddedDependenciesNode(code: string) {
    const added: { [k: string]: any } = {};
    added.label = 'Added dependencies';
    added.text = '';
    added.code = code + '.added';
    return added;
  }

  createDeletedDependenciesNode(code: string) {
    const deleted: { [k: string]: any } = {};
    deleted.label = 'Deleted dependencies';
    deleted.text = '';
    deleted.code = code + '.deleted';
    return deleted;
  }

  createMethodsNode(code: string) {
    const methods: { [k: string]: any } = {};
    methods.label = 'Methods';
    methods.text = '';
    methods.code = code + '.methods';
    return methods;
  }

  /**
   * Build the structure tree. The `value` is the Json object, or a sub-tree of a Json object.
   * The return value is the list of `TreeItemNode`.
   */

  buildDependencyTree(obj: any[], level: string, displayOption: DisplayOption): TreeItemNode[] {
    const treeItemNodes: TreeItemNode[] = [];
    const expLength = (level.replace(/\([^)]*\)/g, '').match(/\./g) || []).length + 1;
    obj.filter(o =>
      (o.code as string).startsWith(level + '.')
      && (o.code.replace(/\([^)]*\)/g, '').match(/\./g) || []).length === expLength
    )
      .forEach(o => {
        const node = new TreeItemNode();
        node.name = o.text;
        node.code = o.code;
        node.path = o.path;
        node.label = o.label;
        node.filterType = o.filterType;
        const children = obj.filter(so => (so.code as string).startsWith(level + '.'));
        if (children && children.length > 0) {
          node.children = this.buildDependencyTree(children, o.code, displayOption);
          // compact middle packages
          if (displayOption === DisplayOption.CompactMiddlePackages && node.filterType === FilterType.Package
            && node.children.length === 1 && node.children[0].filterType === FilterType.Package) {
            node.name = node.name + '.' + node.children[0].name;
            node.path = node.children[0].path;
            node.children = node.children[0].children;
          }
          // flatten packages
          if (displayOption === DisplayOption.FlattenPackages && node.filterType === FilterType.Package) {
            const subpackages = node.children.filter(n => n.filterType === FilterType.Package);
            node.children = node.children.filter(n => (n.filterType !== FilterType.Package));
            subpackages.forEach(sp => {
              const spNode: TreeItemNode = {...node};
              spNode.children = sp.children;
              spNode.path = sp.path;
              spNode.name = spNode.name + '.' + sp.name;
              treeItemNodes.push(spNode);
            });
            // skip adding empty package
            if (node.children.length < 1) {
              return;
            }
          }
          // skip adding branches that dont end on a dependency
          if (node.children.length < 1 && node.filterType !== FilterType.Dependency) {
            return;
          }
          treeItemNodes.push(node);
        }
      });
    return treeItemNodes;
  }

  /**
   * Builds the Graph View of obj
   *
   * @param obj TreeData which will be displayed as a Graph
   * @param displayOption how the graph should be generated
   * @param selectedProject name of the selected project
   *
   * @return a Promise that builds the graph
   */
  async buildGraphView(obj: any[], displayOption: DisplayOption, selectedProject: string): Promise<Network> {
    const idG = new IdGenerator();
    const nodeMap = new Map<string, GraphItem>();
    obj.forEach(node => {
      this.generateNodesFromString(node.code, node.filterType, idG, nodeMap, node.path);
    });
    const projectNode = nodeMap.get(selectedProject);
    for (const key of nodeMap.keys()) {
      if (key.indexOf('.', key.indexOf('.')) === 1 && nodeMap.get(key).type === FilterType.Package) {
        projectNode.children.push(nodeMap.get(key));
      }
    }

    const idMap = this.generateIDMap(nodeMap);
    const resultData = this.generateGraphData(nodeMap);
    const container = document.getElementById('dataview');
    const options = {
      clickToUse: true,
      layout: {
        improvedLayout: false
      },
      interaction: {
        tooltipDelay: 200
      },
      edges: {},
      nodes: {
        shape: 'box'
      }
    };
    return new Promise<Network>((resolve) => {
      const net = new Network(container, resultData, options);
      net.once('afterDrawing', () => {
        document.body.style.cursor = 'default';
        this.clusterToProjects(net, nodeMap);
      });
      this.setClusterRules(net, idMap);
      resolve(net);
    });
  }

  /**
   * Set the event for collapsing the nodes
   *
   * @param net Network
   * @param idMap map with all node ids
   */
  private setClusterRules(net: Network, idMap: Map<number, GraphItem>) {
    net.on('doubleClick', o => {
      if (o.nodes.toString().startsWith('cluster:')) {
        this.openCluster(o.nodes, net);
      } else {
        o.nodes.forEach(entry => {
          this.clusterWithChildren(net, idMap.get(entry));
        });
      }
    });
  }

  /**
   * Open the given cluster node
   *
   * @param node the cluster node that should be opened
   * @param net Network
   */
  private openCluster(node: string, net: Network) {
    const list = net.getNodesInCluster(node);

    if (list.length > 2) {
      net.openCluster(node);
      return;
    }

    net.openCluster(node);
    list.forEach(value => {
      if (value.toString().startsWith('cluster:')) {
        this.openCluster(value.toString(), net);
      }
    });
  }

  /**
   * Cluster the node and its children
   *
   * @param net Network
   * @param node the node that should be clustered
   */
  private clusterWithChildren(net: Network, node: GraphItem) {
    node.children.forEach(value => {
      this.clusterWithChildren(net, value);
    });
    const option = {
      clusterNodeProperties: {
        borderWidth: 3,
        label: node.name,
        title: node.tooltip,
        color: {
          background: this.colorForType(node.type),
          border: '#000000'
        }
      },
      joinCondition(nodeOptions) {
        if (nodeOptions.id.toString().startsWith('cluster:')) {
          return node.hasChildrenInCluster(net, nodeOptions.id.toString());
        }
        return node.id === nodeOptions.id || node.isInChildrenTransitive(nodeOptions.id);
      }
    };
    net.cluster(option);
  }


  /**
   * Generates a map of all node ids
   *
   * @param nodeMap map of all nodes
   */
  private generateIDMap(nodeMap: Map<string, GraphItem>): Map<number, GraphItem> {
    const res = new Map<number, GraphItem>();
    nodeMap.forEach(value => {
      res.set(value.id, value);
    });
    return res;
  }

  /**
   * Process a string to a collection of GraphItems
   *
   * @param nodeString string from which every node is generated
   * @param type what type of node it is
   * @param idG IDGenerator which provides ids for the nodes
   * @param nodeMap map in which every generated node is saved
   * @param tooltip tooltip to be shown when hovering over the node
   * @param depLabel name that should be shown when a dependency node shall be created
   *
   * @return returns the last node that has been created e.g. for java.lang.init() the node for init() is returned
   */
  generateNodesFromString(nodeString: string, type: FilterType, idG: IdGenerator,
                          nodeMap: Map<string, GraphItem>, tooltip: string, depLabel?: string) {
    const codeSet = [];
    // matches everything that is separated with . which isn't in a ()
    nodeString.toString().match(/[^(.)]*(\([^)]*\))?/g).forEach(sub => {
      if (sub !== '' && sub !== undefined) {
        codeSet.push(sub);
      }
    });
    let s = '';
    const root = new GraphItem();
    root.id = -1;
    root.name = s;
    root.code = s;

    let parent = root;

    for (let i = 0; i < codeSet.length; i++) {
      // is needed so that no duplicate nodes are generated
      if (codeSet[i] === 'methods' || (i === 0 && codeSet[i] === 'root')) {
        i++;
      }
      if (s !== '') {
        s += '.';
      }
      s += codeSet[i];
      if (!nodeMap.has(s)) {
        if (codeSet[i] === 'added') {
          const added = this.generateNodesFromString(tooltip !== undefined ? tooltip : codeSet.slice(i + 1).join('.'),
            FilterType.Dependency, idG, nodeMap, tooltip, depLabel);
          added.isDependency = true;
          parent.addedDependency.push(added);
          return parent;
        } else if (codeSet[i] === 'deleted') {
          const deleted = this.generateNodesFromString(tooltip !== undefined ? tooltip : codeSet.slice(i + 1).join('.'),
            FilterType.Dependency, idG, nodeMap, tooltip, depLabel);
          parent.deletedDependency.push(deleted);
          deleted.isDependency = true;
          return parent;
        }

        if (codeSet[i] !== undefined) {
          const node = new GraphItem();
          node.name = depLabel === undefined ? codeSet[i] : depLabel;
          node.id = idG.getNextId();
          node.code = s;
          parent.children.push(node);
          nodeMap.set(s, node);
          parent = node;
        }
      } else {
        parent = nodeMap.get(s);
      }
      if (i === codeSet.length - 1) {
        if (type === FilterType.Dependency) {
          if (parent.type === undefined) {
            parent.type = type;
          }
        } else {
          parent.type = type;
        }
      }
    }
    if (tooltip !== undefined) {
      parent.tooltip = s.replace(/^root./g, '').replace('.methods.', '.');
    }
    return parent;
  }

  /**
   * Process GraphItems into a vis.js format
   *
   * @param nodeMap a Map which contains every node that should be displayed
   *
   * @return Data which vis.js can display (DataSet format)
   */
  generateGraphData(nodeMap: Map<string, GraphItem>) {
    const edgeSet = [];
    const nodeSet = [];
    nodeMap.forEach(node => {
      if (node.name !== undefined && node.shouldBeDisplayed()) {
        node.children.forEach(child => {
          edgeSet.push({from: child.id, to: node.id, arrows: 'to'});
        });

        node.addedDependency.forEach(dep => {
          edgeSet.push({from: node.id, to: dep.id, label: 'Added', arrows: 'to', color: '#cc2222', width: 3});
        });

        node.deletedDependency.forEach(dep => {
          edgeSet.push({from: node.id, to: dep.id, label: 'Deleted', arrows: 'to', color: '#22cc22', width: 3});
        });

        nodeSet.push({id: node.id, label: node.name, title: node.tooltip, color: this.colorForType(node.type)});
      }
    });

    return {edges: edgeSet, nodes: nodeSet};
  }

  /**
   * Gets the corresponding color to a node type
   *
   * @param type node type
   *
   * @return Color to be displayed
   */
  colorForType(type: FilterType): string {
    switch (type) {
      case FilterType.Project:
        return '#ffdf70';
      case FilterType.Package:
        return '#57c7e3';
      case FilterType.Class:
        return '#d9c8ae';
      case FilterType.Method:
        return '#f79767';
      case FilterType.Dependency:
        return '#6dcf9e';
    }
    return '#c990c0';
  }


  public filter(filterText: string) {
    let filteredTreeData;
    const splitFilterText = filterText.split(':');

    const filterType = (filterText.match(/^[pcmd]:/) !== null) ? splitFilterText[0] : null;
    filterText = (splitFilterText.length > 1) ? splitFilterText[1] : splitFilterText[0];
    if (filterText && filterText.length > 0) {
      filteredTreeData = this.treeData.filter(d => ((filterType === null || d.filterType === filterType)
        && d.text.toLocaleLowerCase().indexOf(filterText.toLocaleLowerCase()) > -1));

      filteredTreeData.forEach(ftd => {
        // load nodes parent nodes of the found nodes
        this.addParentNodes(filteredTreeData, (ftd.code as string));
        // load child nodes
        this.addChildNodes(filteredTreeData, ftd.code, filterType);
      });

    } else {
      filteredTreeData = this.treeData;
    }

    // Built the filtered tree
    const data = this.buildDependencyTree(filteredTreeData, this.root, this.selectedDisplayOption);
    // Notify the change.
    this.dataChange.next(data);
  }

  private loadProjectName() {
    const queryProjectNames = 'MATCH (p:ProjectInformation) WHERE p.internal=true RETURN p.name;';
    this.neo4j.run(queryProjectNames).then(projectNames => {
      this.projectNames = projectNames;
    });
  }

  private addParentNodes(filteredTreeData: any[], codeString: string) {
    while (codeString.lastIndexOf('.') > -1) {
      const index = codeString.lastIndexOf('.');
      codeString = codeString.substring(0, index);
      if (filteredTreeData.findIndex(t => t.code === codeString) === -1) {
        const obj = this.treeData.find(d => d.code === codeString);
        if (obj) {
          filteredTreeData.push(obj);
        }
      }
    }
  }

  private addChildNodes(filteredTreeData: any[], code: string, filterType: string) {
    if (filterType !== FilterType.Dependency) {
      this.treeData.filter(n => (n.code !== code)
        && (n.code.indexOf(code + '.') > -1)).forEach(child => {
        if (filteredTreeData.findIndex(n => n.code === child.code) === -1) {
          filteredTreeData.push(child);
        }
      });
    }
  }

  private clusterToProjects(net: Network, nodeMap: Map<string, GraphItem>) {
    const list: string[] = [];
    for (const key of nodeMap.keys()) {
      if (key.indexOf('.') === -1) {
        this.clusterWithChildren(net, nodeMap.get(key));
      }
    }
  }
}

/**
 * @title Dependency tree
 */
@Component({
  selector: 'app-dependencytree',
  templateUrl: 'dependencytree.component.html',
  styleUrls: ['dependencytree.component.css'],
  providers: [DependencyTreeDatabase, CookieService]
})
export class DependencytreeComponent implements OnInit {


  /** Map from flat node to nested node. This helps us finding the nested node to be modified */
  flatNodeMap = new Map<TreeItemFlatNode, TreeItemNode>();
  /** Map from nested node to flattened node. This helps us to keep the same object for selection */
  nestedNodeMap = new Map<TreeItemNode, TreeItemFlatNode>();
  treeControl: FlatTreeControl<TreeItemFlatNode>;
  treeFlattener: MatTreeFlattener<TreeItemNode, TreeItemFlatNode>;
  dataSource: MatTreeFlatDataSource<TreeItemNode, TreeItemFlatNode>;
  @ViewChild('searchField', {static: false}) searchField;
  selectedProject: string;
  projectVersion: string;
  filterText = '';
  private db: DependencyTreeDatabase;
  graph: Network;
  graphVisible = false;
  graphWarning: MatSnackBarRef<SimpleSnackBar>;

  constructor(public snackbar: MatSnackBar, private database: DependencyTreeDatabase, private cookieService: CookieService) {

    this.treeFlattener = new MatTreeFlattener(this.transformer, this.getLevel,
      this.isExpandable, this.getChildren);
    this.treeControl = new FlatTreeControl<TreeItemFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
    this.db = database;
    database.dataChange.subscribe(data => {
      this.dataSource.data = data;
    });

  }

  get FilterType() {
    return FilterType;
  }

  get DisplayOption() {
    return DisplayOption;
  }

  ngOnInit() {
    this.loadCookies();
  }

  getLevel = (node: TreeItemFlatNode) => node.level;

  isExpandable = (node: TreeItemFlatNode) => node.expandable;

  getChildren = (node: TreeItemNode): TreeItemNode[] => node.children;

  hasChild = (_: number, nodeData: TreeItemFlatNode) => nodeData.expandable;

  /** Transformer to convert nested node to flat node. Record the nodes in maps for later use. */
  transformer = (node: TreeItemNode, level: number) => {
    const existingNode = this.nestedNodeMap.get(node);
    const flatNode = existingNode && existingNode.name === node.name
      ? existingNode
      : new TreeItemFlatNode();
    flatNode.name = node.name;
    flatNode.level = level;
    flatNode.code = node.code;
    flatNode.path = node.path;
    flatNode.label = node.label;
    flatNode.filterType = node.filterType;
    flatNode.expandable = node.children && node.children.length > 0;

    this.flatNodeMap.set(flatNode, node);
    this.nestedNodeMap.set(node, flatNode);
    return flatNode;
  }

  /** Event-Handler changes displayed Changelog */
  changeDependencyTree(value) {
    this.db.loadChangelogFromDatabase(this.database.selectedProject, value);
  }

  /** Event-Handler triggered then input text in search field changes */
  filterChanged(event: Event) {
    const filterText = ((event.target) as HTMLInputElement).value;
    this.database.filter(filterText);
    if (filterText) {
      this.treeControl.expandAll();
    } else {
      this.treeControl.collapseAll();
    }
  }

  /** Event-Handler triggered then displayOption is selected */
  changeDisplayOption(displayOption: DisplayOption) {
    // Built tree with selected display option
    const data = this.database.buildDependencyTree(this.database.treeData, this.database.root, displayOption);
    this.database.selectedDisplayOption = displayOption;
    this.database.dataChange.next(data);
  }

  /** Event-Handler triggered by Filter selection, adds the selected FilterType into the search field */
  searchFilterSelected(selectedFilter: FilterType) {
    if (this.searchField.nativeElement.value.match(/^[pcmd]:/) !== null) {
      this.searchField.nativeElement.value = selectedFilter + this.searchField.nativeElement.value.substr(1);
    } else {
      this.searchField.nativeElement.value = selectedFilter + ':' + this.searchField.nativeElement.value;
    }
    this.searchField.nativeElement.dispatchEvent(new Event('input'));
  }

  /** Event-Handler triggered by project selection, loads the available changelog version */
  loadProjectVersion(projectName: string) {
    this.db.loadChangelogIds(projectName);
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any) {
    this.saveToCookies();
  }

  private loadCookies() {
    let fText = '';
    if (this.cookieService.check('projectName')) {
      const pName = this.cookieService.get('projectName').toString();
      if (pName !== undefined) {
        this.selectedProject = pName;
        this.loadProjectVersion(this.selectedProject);
      }
    }
    if (this.cookieService.check('displayOption')) {
      const dOption = reverseDisplayOption.get(this.cookieService.get('displayOption').toString());
      if (dOption !== undefined) {
        this.db.selectedDisplayOption = dOption;
      }
    }
    if (this.cookieService.check('filterText')) {
      fText = this.cookieService.get('filterText');
      if (fText !== undefined) {
        this.filterText = fText;
      }
    }
    if (this.cookieService.check('projectVersion')) {
      const pVersion = this.cookieService.get('projectVersion');
      if (pVersion !== undefined) {
        this.projectVersion = pVersion;
        this.db.loadChangelogFromDatabase(this.db.selectedProject, pVersion)
          .then(() => this.searchField.nativeElement.dispatchEvent(new Event('input')));
      }
    }

  }

  private saveToCookies() {
    this.cookieService.set('filterText', this.filterText, 365, '/', '', false, 'Strict');
    this.cookieService.set('projectVersion', this.projectVersion, 365, '/', '', false, 'Strict');
    this.cookieService.set('projectName', this.selectedProject, 365, '/', '', false, 'Strict');
    this.cookieService.set('displayOption', this.db.selectedDisplayOption.toString(), 365, '/', '', false, 'Strict');
  }

  private reset() {
    this.selectedProject = '';
    this.projectVersion = '';
    this.filterText = '';
    this.db.selectedDisplayOption = DisplayOption.CompactMiddlePackages;
    this.db.treeData = [];
    const data = this.db.buildDependencyTree(this.db.treeData, this.db.root, this.db.selectedDisplayOption);
    this.db.dataChange.next(data);
  }

  /**
   * Function for generating the Graph View
   *
   * @param displayOption not implemented
   */
  async generateGraphView(displayOption: DisplayOption) {
    if (this.graph === undefined || this.graph === null) {
      document.body.style.cursor = 'wait';
      this.graph = await this.database.buildGraphView(this.database.treeData, displayOption, this.selectedProject);
    }

    const options = new MatSnackBarConfig();
    options.horizontalPosition = 'end';
    this.graph.once('click', () => {
      this.graphWarning = this.snackbar.open('to end the focus on the graph press ESC or click into the menu pane', undefined, options);
    });
    this.graphVisible = true;
  }

  /**
   * Delete Snackbar and Legend when changing to ListView
   */
  changeToList() {
    this.graphVisible = false;
    if (this.graphWarning !== undefined) {
      this.graphWarning.dismiss();
    }
  }
}

