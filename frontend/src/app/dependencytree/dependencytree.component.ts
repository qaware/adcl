import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, Injectable} from '@angular/core';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {BehaviorSubject, forkJoin} from 'rxjs';
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
    const data = this.buildDependencyTree(this.treeData, 'root');
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
      'return dm.name, toString(m.isConstructor), dm.changeStatus, m.name';


    const packageInformation: any[] = [{text: '', code: 'root'}];
    const classInformation: any[] = [];
    const methodInformation: any[] = [];
    const dependencyInformationMethod: any[] = [];
    const root = 'root';
    // executing the queries and saving the data in appropriate way for display in tree view
    const packageResult = this.neo4j.run(queryPackageInformation, params).then(packageInfo => {
      packageInfo.forEach(pInfo => {
        let code = root;
        const splitPackageName = (pInfo[0] as string).toString().split('.');
        splitPackageName.forEach((pn) => {
          code = code + '.' + pn;
          if (packageInformation.find(e => e.code === code) === undefined) {
            const obj: { [k: string]: any } = {};
            obj.text = pn;
            obj.code = code;
            code = obj.code;
            obj.label = 'Package';
            packageInformation.push(obj);
        }
        });
      });
    });

    const classResult = this.neo4j.run(queryClassInformation, params).then(classInfo => {
      classInfo.forEach(cInfo => {
        const obj: { [k: string]: any } = {};
        obj.text = cInfo[0].match( /[^.]*$/)[0];
        obj.code = root + '.' + cInfo[0];
        obj.service = cInfo[1];
        obj.package = cInfo[2];
        obj.label = 'Class';
        classInformation.push(obj);
      });
    });

    const methodResult = this.neo4j.run(queryMethodInformation, params).then(methodInfo => {
      methodInfo.forEach(mInfo => {
        const obj: { [k: string]: any } = {};
        obj.text = mInfo[0].match(/\w*\(.*\)/)[0];
        obj.code = root + '.' + mInfo[2] + '.' + obj.text;
        obj.constructor = mInfo[1];
        obj.className = mInfo[2];
        obj.label = 'Method';
        methodInformation.push(obj);

        const added: { [k: string]: any } = {};
        added.label = 'Added dependencies';
        added.text = '';
        added.code = obj.code + '.added';
        methodInformation.push(added);
        const deleted: { [k: string]: any } = {};
        deleted.label = 'Deleted dependencies';
        deleted.text = '';
        deleted.code = obj.code + '.deleted';
        methodInformation.push(deleted);
      });
    });

    const dependencyMethodResult = this.neo4j.run(queryDependencyInformationMethod, params).then(dependencyClassInfo => {
      dependencyClassInfo.forEach(dcInfo => {
        const status = (dcInfo[2] === 'ADDED') ? '.added' : '.deleted';
        const obj: { [k: string]: any } = {};
        obj.text = dcInfo[0];
        obj.code = root + '.' + ((dcInfo[1] === 'true') ? dcInfo[3].split('(')[0] + '.' +
          dcInfo[3].match(/\w*\(.*\)/)[0] : dcInfo[3]) + status + '.' + 0;
        obj.changeStatus = dcInfo[2];
        obj.usedByMethod = dcInfo[3];
        obj.label = (status === '.added') ? '🔒 Dependency' : '🔓 Dependency';
        dependencyInformationMethod.push(obj);
      });
    });
    // wait for all query results before building the tree
    forkJoin(packageResult, classResult, methodResult, dependencyMethodResult).subscribe(_ => {
      this.treeData = [...packageInformation, ...classInformation, ...methodInformation, ...dependencyInformationMethod];
      console.log(this.treeData);
      const data = this.buildDependencyTree(this.treeData, 'root');
      this.dataChange.next(data);
    });
  }

  /**
   * Build the structure tree. The `value` is the Json object, or a sub-tree of a Json object.
   * The return value is the list of `changelogItemNode`.
   */

  buildDependencyTree(obj: any[], level: string): TreeItemNode[] {
    return obj.filter(o =>
      (o.code as string).startsWith(level + '.')
      && (o.code.replace(/\(.*\)/g, '').match(/\./g) || []).length === (level.replace(/\(.*\)/g, '').match(/\./g) || []).length + 1
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
    const data = this.buildDependencyTree(filteredTreeData, 'root');
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
  }

  filterChanged(filterText: string) {
    this.database.filter(filterText);
    if (filterText) {
      this.treeControl.expandAll();
    } else {
      this.treeControl.collapseAll();
    }
  }
}
