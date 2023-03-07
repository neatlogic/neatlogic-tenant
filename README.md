## 关于

neatlogic-tenant是neatlogic-framework的业务逻辑模块，提供neatlogic各种基础服务，包括用户管理、权限管理、矩阵管理、集成管理、表单管理、数据仓库、接口审计等。

## 主要功能

### 自定义表单

自定义表单目前主要是neatlogic-process的itsm功能使用，可以通过零代码的方式实现大部分复杂的表单应用场景。

- 原生支持多种表单组件。
  ![img1](https://github.com/neatlogic/.github/blob/main/images/form/img.png?raw=true)

- 支持通过配置方式实现控件之间交互联动
  ![img1](https://github.com/neatlogic/.github/blob/main/images/form/img_1.png?raw=true)
- 支持多场景，每个场景可以重新设置表单组件的配置，结合工作流引擎，可以实现不同流程步骤关联不同的表单场景。
- 支持低代码方式配置自定义组件。
  ![img1](https://github.com/neatlogic/.github/blob/main/images/form/img_2.png?raw=true)

### 集成管理

集成管理用于集中管理外部接口，所有对第三方接口的调用都需要在这里配置。

- 支持编写javascript对输入输出参数进行结构转换，让外部接口和内部功能可以无缝对接。
  ![img1](https://github.com/neatlogic/.github/blob/main/images/integration/img_1.png?raw=true)
- 具备审计功能。
  ![img1](https://github.com/neatlogic/.github/blob/main/images/integration/img_2.png?raw=true)

### 数据仓库

数据仓库用于把数据通过查询语句抽出来，存放到动态生成的数据表里，满足频繁的查询场景，例如报表、大屏、仪表板等。
![img1](https://github.com/neatlogic/.github/blob/main/images/datawarehouse/img.png?raw=true)

### 矩阵

矩阵用于把数据转换成二维表形式，为其他功能提供数据源服务，例如自定义表单。支持自定义矩阵（自行编辑属性）、集成矩阵（把集成中的接口转换成矩阵）、视图矩阵（通过SQL语句重新组装数据）等。
![img1](https://github.com/neatlogic/.github/blob/main/images/matrix/img.png?raw=true)