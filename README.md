# ibatis2Mybatis完整步骤【附脚本】 


----------

实习的公司后台采用的ibatis，老大让我重构成Mybatis，于是弄清楚了整个流程，梳理如下，并写好了[脚本-java代码](https://github.com/changfubai/ibatis2mybatis3/blob/master/src/main/java/com/changfubai/ibatis2Mybatis/Ibatis2Mybatus.java)，自动处理daoImp.java、sqlMapConfig.xml、sqlMap.xml文件，使用时指定对应的包或单个文件的绝对路径即可(对应main中变量daoImplDirs、configXMLDirs、sqlMapXMLDirs)。部分业务处理需要手动修改或自行添加正则匹配。请按步骤食用~

----------


## step1: 添加maven包


> 具体版本根据业务需求调整，参见[仓库](http://mvnrepository.com/)

	<dependency>
	    <groupId>org.mybatis</groupId>
	    <artifactId>mybatis-spring</artifactId>
	    <version>1.2.1</version>
	</dependency>
	<dependency>
	    <groupId>org.mybatis</groupId>
	    <artifactId>mybatis</artifactId>
	    <version>3.2.8</version>
	</dependency>

## step2: 处理ibatis daoImpl实现类

需要替换如下内容：


- SqlMapClientDaoSupport ---------- SqlSessionDaoSupport   
- getSqlMapClient ---------- getSqlSession
- queryForObject ---------- selectOne
- queryForList ---------- selectList
- 其他业务逻辑处理

这部分运行脚本即可，[点击这里](https://github.com/changfubai/ibatis2mybatis3)查看，只有一个Ibatis2Mybatus.java，指定main方法中daoImplDirs变量为要处理的java类或者多个类的父文件夹的绝对路径即可。  

> 其他业务逻辑可以在transportDaoFile方法中添加正则匹配（约79行处）

处理完编译一下，修改报错的地方即可。

## step3: 处理Spring配置文件的bean注入

需要替换如下内容：

- "sqlMapClient" ---------- "sqlSessionFactory" 【引号带上避免命名冲突】- 此处是daoImpl实现类继承SqlSessionDaoSupport需要注入的构造参数
- org.springframework.orm.ibatis.SqlMapClientFactoryBean ---------- org.mybatis.spring.SqlSessionFactoryBean

此处配置文件太灵活了，没有写脚本处理，在Spring配置文件全局替换即可~ 【IDEA ctrl+shift+r】

## step4: 处理sqlMapConfig.xml 

该文件指定了ibatis的sqlMap配置文件信息，typeHandler，TypeAlias等

需要替换如下内容：

- 命名空间,Qname等
	- sqlMapConfig ---------- configuration
	- -//iBATIS.com//DTD SQL Map  Config 2.0//EN ---------- -//mybatis.org//DTD Config 3.0//EN
	- http://ibatis.apache.org/dtd/sql-map-config-2.dtd ---------- http://mybatis.org/dtd/mybatis-3-config.dtd
- 根标签
	- sqlMapConfig ---------- configuration
- 子标签 typeAlias、typeHandler、sqlMap
	- typeAlias ---------- 被typeAliases 标签包裹
	- typeHandler ---------- 被typeHandlers 标签包裹
		- callback ---------- handler
	- sqlMap ---------- 被mappers包裹
	- sqlMap ---------- mapper

看起来有点复杂，我在脚本中处理了，指定单个配置文件或多个配置文件的父文件夹的绝对路径即可，对应变量configXMLDirs。

> 说明： 未处理setting等标签，可自行添加正则或者手动修改，这部分IDEA会报错，根据提示修改即可，若没有报错，则没有问题了。

## step5: 处理sqlMap.xml

这部分文件特别多，建议直接执行代码

需要替换如下内容(代码做了的事情)：

- 命名空间,Qname等
	- sqlMap ---------- mapper
	- -//ibatis.apache.org//DTD SQL Map 2.0//EN ---------- -//mybatis.org//DTD Mapper 3.0//EN
	- http://ibatis.apache.org/dtd/sql-map-2.dtd ---------- http://mybatis.org/dtd/mybatis-3-mapper.dtd
- 根标签
	- sqlMap ---------- mapper
- 子标签resultMap、select、update、delete、insert
	- resultMap 
		- class ---------- type
		- result子标签中属性： jdbcType 直接删除
	- select、update、delete、insert的属性：
		- resultClass ---------- resultType
		- parameterClass ---------- parameterType
	- 关于sql 
		- like '%$name$%' ---------- like concat('%', #{name}, '%')
		- like '$name$%' ---------- like concat('%', #{name}, '%')
		- #name# ---------- #{name}#
		- $name$ ---------- ${name}$
	- 关于条件处理
		- <isNotNull property="name"  prepend="and"\>xxx</isNotNull\> ---------- <if test="name!=null"\> and xxx </if\>
		-  <isNotNull property="name"\>xxx</isNotNull\> ---------- <if test="name!=null"\> xxx </if\>
		-  isNull 同上处理
		-  <isEqual property="name" compareValue="xiaobai"\>xxx</isEqual\> ---------- <if test="name==xiaobai"\>xxx</if\>
		-  isNotEqual同上处理
		-  更多业务处理自行添加正则匹配即可，在handleSqlTextByPattern方法中（约373行）


还需要手动处理的：

- dynamic 标签，因业务场景不同，需要手动处理，若存在该标签IDEA会报错的~
- in 查询写成这样的 in ($name$) 脚本插入了错误的标签，使得IDEA可以报错，避免非编译错误改bug
- iterate 标签没有改，手动来或者写一下正则匹配即可。

## step6: 自定义TypeHandler

ibatis实现TypeHandlerCallback接口，mybatis改成TypeHandler<T>，根据业务需求自己修改即可~ 如果类比较多，考虑适配器模式或者模板方法模式即可(加反射机制)

这一步没改的话，运行会报错~

## step7: 去除ibatis依赖~

好处是简化项目依赖，同时，在去除包后，如果有改漏的编译不会通过~

接下来就是运行测试了~

mail:changfubai96@gamil.com
blog:http://blog.changfubai.com
