import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, HostListener, Injectable, OnInit, ViewChild} from '@angular/core';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {BehaviorSubject, forkJoin} from 'rxjs';
import {AngularNeo4jService} from 'angular-neo4j';
import {environment} from '../../environments/environment';
import {CookieService} from 'ngx-cookie-service';

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

/** Filter types */
export enum FilterType {
  Package = 'p',
  Class = 'c',
  Method = 'm',
  Dependency = 'd'
}

/** Display options */
export enum DisplayOption {
  Standard = 'Normal',
  CompactMiddlePackages = 'Compact Middle Packages',
  FlattenPackages = 'Flat Packages'
}

const reverseDisplayOption = new Map<string, DisplayOption>();
reverseDisplayOption.set('Normal', DisplayOption.Standard);
reverseDisplayOption.set('Compact Middle Packages', DisplayOption.CompactMiddlePackages);
reverseDisplayOption.set('Flat Packages', DisplayOption.FlattenPackages);

/** Display Label */
export enum Label {
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
    const queryTree = 'match p=(r:ProjectInformation{name: {pName}})<-[:Parent *]-(i:Information)' +
      'where single (r in relationships(p) where any(x in keys(r) where x = "versionInfo.' + version + '")) ' +
      'or all (r in relationships(p) where none(x in keys(r) where x starts with "versionInfo"))' +
      'return i.path, i.name, labels(i) as labels';

    // Query fetching all dependencies
    const queryDependencies = 'match p=(r:ProjectInformation{name: {pName}})<-[:Parent *]-' +
      '(i:Information)-[:MethodDependency|ClassDependency|PackageDependency|ProjectDependency]->(di)' +
      'where single (r in relationships(p) where any(x in keys(r) where x = "versionInfo.' + version + '")) ' +
      'return di.path as path, di.name as name, labels(di) as diLabels,' +
      ' any(x in relationships(p) where x["versionInfo.' + version + '"]) ' +
      'as Changestatus, i.path as iPath, labels(i) as iLabels, i.name as iName';

    const treeResult = this.neo4j.run(queryTree, params).then(nodes => {
      nodes.forEach(node => {
        const path: string = (node[0] as string).substr((projectName.toString()).length + 1);
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
    });

    const dependencyResult = this.neo4j.run(queryDependencies, params).then(nodes => {
      nodes.forEach(node => {
        const path = (node[0] as string).substr((projectName.toString()).length + 1);
        const name = node[1];
        const type = this.resolveType(node[2]);
        const usedByType = this.resolveType(node[5]);
        const usedByName = node[6];
        const status = (node[3] === true) ? 'added' : 'deleted';
        const usedByPath = (node[4] as string).substr((projectName.toString()).length + 1);
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
            dependencyClass.push(cpy);
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
    obj.filter(o =>
      (o.code as string).startsWith(level + '.')
      && (o.code.replace(/\([^)]*\)/g, '').match(/\./g) || []).length === (level.replace(/\([^)]*\)/g, '').match(/\./g) || []).length + 1
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

  constructor(private database: DependencyTreeDatabase, private cookieService: CookieService) {

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
        this.db.loadChangelogFromDatabase(this.db.selectedProject, pVersion).
        then(v => this.searchField.nativeElement.dispatchEvent(new Event('input')));
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
}

