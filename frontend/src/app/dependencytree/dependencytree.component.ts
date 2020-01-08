import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, Injectable} from '@angular/core';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {BehaviorSubject} from 'rxjs';
import {AngularNeo4jService} from 'angular-neo4j';
import {environment} from '../../environments/environment';

/**
 * Node for Tree item
 */
export class TreeItemNode {
  children: TreeItemNode[];
  item: string;
  code: string;
  label: string;
}

/** Flat  item node with expandable and level information */
export class TreeItemFlatNode {
  item: string;
  label: string;
  level: number;
  expandable: boolean;
  code: string;
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

  constructor(private neo4j: AngularNeo4jService) {
    this.initialize();
  }

  get data(): TreeItemNode[] {
    return this.dataChange.value;
  }

  initialize() {
    this.treeData = [];
    // Build the tree nodes from Json object. The result is a list of `changelog notes` with nested
    this.connectToDatabase();
    this.loadChangelogIds();
    const data = this.buildDependencyTree(this.treeData, '0');
    // Notify the change.
    this.dataChange.next(data);
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
  loadChangelogIds() {
    const queryChangelogId = 'MATCH (b:VersionInformation)-[:BEFORE]-(n:ChangelogInformation)-[:AFTER]-(a:VersionInformation) ' +
      'RETURN b.versionName +\'-\'+ a.versionName;';
    this.neo4j.run(queryChangelogId).then(changelogInformationIds => {
      this.changelogIds = changelogInformationIds;
    });
  }

  /**
   * Load the changelog data for the selected changelog identifier
   * @param value the changelog identifier
   */
  loadChangelogFromDatabase(value) {
    // split the identifier for use in queries
    const split = (value as string).toString().split('-', 2);
    const params = {before: split[0], after: split[1]};

    // Query fetching the package information related to the changelog
    const queryPackageInformation = 'MATCH (b:VersionInformation{versionName: {before}})-[:BEFORE]-(n:ChangelogInformation)-[:AFTER]-' +
      '(a:VersionInformation{versionName: {after}}) WITH ' +
      'n MATCH (p:PackageInformation)-[:CHANGELOG]-(n:ChangelogInformation) RETURN p.packageName';

    // Query fetching the class information related to the changelog
    const queryClassInformation = 'MATCH (b:VersionInformation{versionName: {before}})-[:BEFORE]-(n:ChangelogInformation)-[:AFTER]-' +
      '(a:VersionInformation{versionName: {after}}) WITH n MATCH (p:PackageInformation)-[:CHANGELOG]-(n:ChangelogInformation) ' +
      'WITH p MATCH (c:ClassInformation)-[r:IS_CLASS_OF]->(p:PackageInformation) return c.className, toString(c.isService), p.packageName;';

    // Query fetching the method information related to the changelog
    const queryMethodInformation = 'MATCH (b:VersionInformation{versionName: {before}})-[:BEFORE]-(n:ChangelogInformation)-[:AFTER]-' +
      '(a:VersionInformation{versionName: {after}}) WITH n MATCH (p:PackageInformation)-[:CHANGELOG]-(n:ChangelogInformation) ' +
      'WITH p MATCH (c:ClassInformation)-[r:IS_CLASS_OF]->(p:PackageInformation) ' +
      'WITH c MATCH (m:MethodInformation)-[r:IS_METHOD_OF]->(c:ClassInformation) return m.name, toString(m.isConstructor), c.className;';

    // Query fetching the dependency information regarding methods related to the changelog
    const queryDependencyInformationMethod = 'MATCH (b:VersionInformation{versionName: {before}})' +
      '-[:BEFORE]-(n:ChangelogInformation)-[:AFTER]-' +
      '(a:VersionInformation{versionName: {after}}) WITH n MATCH (p:PackageInformation)-[:CHANGELOG]-(n:ChangelogInformation) ' +
      'WITH p MATCH (c:ClassInformation)-[r:IS_CLASS_OF]->(p:PackageInformation) ' +
      'WITH c MATCH (m:MethodInformation)-[r:IS_METHOD_OF]->(c:ClassInformation) ' +
      'WITH m MATCH (dm:ChangelogDependencyInformation)<-[r:USES]-(m:MethodInformation)' +
      'return dm.name, toString(dm.isConstructor), dm.changeStatus, m.name';


    const packageInformation: any[] = [{text: '', code: '0'}];
    const classInformation: any[] = [];
    const methodInformation: any[] = [];
    const dependencyInformationMethod: any[] = [];

    // executing the queries and saving the data in appropriate way for display in tree view
    this.neo4j.run(queryPackageInformation, params).then(packageInfo => {
      let index = 1;
      packageInfo.forEach(pInfo => {
        let code = '0';
        let name = '';
        const splitPackageName = (pInfo[0] as string).toString().split('.');
        splitPackageName.forEach((pn) => {
            if (!Boolean(packageInformation.find((element) => (element.text === (name + pn))))) {
              code = packageInformation.find((element) => {
                return (element.text === name.substr(0, name.length - 1));
              }).code;
              const obj: { [k: string]: any } = {};
              obj.text = name + pn;
              name = obj.text + '.';
              obj.code = code + '.' + index++;
              code = obj.code;
              obj.label = 'Package';
              packageInformation.push(obj);
            } else {
              name = name + pn + '.';
            }
          }
        );
      });
      console.log(packageInformation);
    }).then(() => {
      this.neo4j.run(queryClassInformation, params).then(classInfo => {
          let index = 1;
          classInfo.forEach(cInfo => {
            const obj: { [k: string]: any } = {};
            obj.text = cInfo[0];
            const pCode = packageInformation.filter((element) => {
              return (element.text === cInfo[2]);
            });
            obj.code = pCode[0].code + '.' + index++;
            obj.service = cInfo[1];
            obj.package = cInfo[2];
            obj.label = 'Class';
            classInformation.push(obj);
          });
        }
      ).then(() => {
        this.neo4j.run(queryMethodInformation, params).then(methodInfo => {
          let index = 1;
          methodInfo.forEach(mInfo => {
            const obj: { [k: string]: any } = {};
            obj.text = mInfo[0];
            const cCode = classInformation.filter((element) => {
              return (element.text === mInfo[2]);
            });
            obj.code = cCode[0].code + '.' + index++;
            obj.constructor = mInfo[1];
            obj.className = mInfo[2];
            obj.label = 'Method';
            methodInformation.push(obj);

            const added: { [k: string]: any } = {};
            added.label = 'Added dependencies to';
            added.code = obj.code + '.0';
            methodInformation.push(added);
            const deleted: { [k: string]: any } = {};
            deleted.label = 'Deleted dependencies to';
            deleted.code = obj.code + '.1';
            methodInformation.push(deleted);
          });
        }).then(() => {
          this.neo4j.run(queryDependencyInformationMethod, params).then(dependencyClassInfo => {
              let index = 1;
              dependencyClassInfo.forEach(dcInfo => {
                const obj: { [k: string]: any } = {};
                obj.text = dcInfo[0];
                const mCode = methodInformation.filter((element) => {
                  return (element.text === dcInfo[3]);
                });
                const status = (dcInfo[2] === 'ADDED') ? '.0' : '.1';
                obj.code = mCode[0].code + status + '.' + index++;
                obj.constructor = dcInfo[1];
                obj.changeStatus = dcInfo[2];
                obj.usedByMethod = dcInfo[3];
                obj.label = 'Dependency';
                dependencyInformationMethod.push(obj);
              });
            }
          ).then(() => {
              this.treeData = [...packageInformation, ...classInformation, ...methodInformation, ...dependencyInformationMethod];
              const data = this.buildDependencyTree(this.treeData, '0');
              this.dataChange.next(data);
            }
          );
        });
      });
    });
  }

  /**
   * Build the structure tree. The `value` is the Json object, or a sub-tree of a Json object.
   * The return value is the list of `changelogItemNode`.
   */

  buildDependencyTree(obj: any[], level: string): TreeItemNode[] {
    return obj.filter(o =>
      (o.code as string).startsWith(level + '.')
      && (o.code.match(/\./g) || []).length === (level.match(/\./g) || []).length + 1
    )
      .map(o => {
        const node = new TreeItemNode();
        node.item = o.text;
        node.code = o.code;
        node.label = o.label;
        const children = obj.filter(so => (so.code as string).startsWith(level + '.'));
        if (children && children.length > 0) {
          node.children = this.buildDependencyTree(children, o.code);
        }
        return node;
      });
  }

  public filter(filterText: string) {
    let filteredTreeData;
    if (filterText) {
      filteredTreeData = this.treeData.filter(d => d.text.toLocaleLowerCase().indexOf(filterText.toLocaleLowerCase()) > -1);
      Object.assign([], filteredTreeData).forEach(ftd => {
        let str = (ftd.code as string);
        while (str.lastIndexOf('.') > -1) {
          const index = str.lastIndexOf('.');
          str = str.substring(0, index);
          if (filteredTreeData.findIndex(t => t.code === str) === -1) {
            const obj = this.treeData.find(d => d.code === str);
            if (obj) {
              filteredTreeData.push(obj);
            }
          }
        }
      });
    } else {
      filteredTreeData = this.treeData;
    }
    // Build the tree nodes from Json object. The result is a list of `TodoItemNode` with nested
    // file node as children.
    const data = this.buildDependencyTree(filteredTreeData, '0');
    // Notify the change.
    this.dataChange.next(data);
  }
}

/**
 * @title Dependency tree
 */
@Component({
  selector: 'app-dependencytree',
  templateUrl: 'dependencytree.component.html',
  styleUrls: ['dependencytree.component.css'],
  providers: [DependencyTreeDatabase]
})
export class DependencytreeComponent {
  /** Map from flat node to nested node. This helps us finding the nested node to be modified */
  flatNodeMap = new Map<TreeItemFlatNode, TreeItemNode>();

  /** Map from nested node to flattened node. This helps us to keep the same object for selection */
  nestedNodeMap = new Map<TreeItemNode, TreeItemFlatNode>();

  treeControl: FlatTreeControl<TreeItemFlatNode>;

  treeFlattener: MatTreeFlattener<TreeItemNode, TreeItemFlatNode>;

  dataSource: MatTreeFlatDataSource<TreeItemNode, TreeItemFlatNode>;

  private db: DependencyTreeDatabase;

  constructor(private database: DependencyTreeDatabase) {
    this.treeFlattener = new MatTreeFlattener(this.transformer, this.getLevel,
      this.isExpandable, this.getChildren);
    this.treeControl = new FlatTreeControl<TreeItemFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
    this.db = database;
    database.dataChange.subscribe(data => {
      this.dataSource.data = data;
    });
  }

  getLevel = (node: TreeItemFlatNode) => node.level;

  isExpandable = (node: TreeItemFlatNode) => node.expandable;

  getChildren = (node: TreeItemNode): TreeItemNode[] => node.children;

  hasChild = (_: number, nodeData: TreeItemFlatNode) => nodeData.expandable;

  changeDependencyTree(value) {
    this.db.loadChangelogFromDatabase(value);
  }

  /**
   * Transformer to convert nested node to flat node. Record the nodes in maps for later use.
   */
  transformer = (node: TreeItemNode, level: number) => {
    const existingNode = this.nestedNodeMap.get(node);
    const flatNode = existingNode && existingNode.item === node.item
      ? existingNode
      : new TreeItemFlatNode();
    flatNode.item = node.item;
    flatNode.level = level;
    flatNode.code = node.code;
    flatNode.label = node.label;
    flatNode.expandable = node.children && node.children.length > 0;
    this.flatNodeMap.set(flatNode, node);
    this.nestedNodeMap.set(node, flatNode);
    return flatNode;
  };

  filterChanged(filterText: string) {
    this.database.filter(filterText);
    if (filterText) {
      this.treeControl.expandAll();
    } else {
      this.treeControl.collapseAll();
    }
  }
}
