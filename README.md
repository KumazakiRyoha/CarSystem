# 汽车租赁项目开发总结

----------

## 1. SSM项目的环境配置
<font size=4>使用的是applicaton-*.xml的形式将DAO层、Service层、Transaction层分开配置，这样便于管理。```application-dao.xm```和```application-service.xml```分别对应数据层控制、业务逻辑service控制和事务的控制。
###1.1  数据库持久层相关配置(```application-dao.xm```)
首先对数据连接进行配置：<br>
引入数据库配置文件后,使用```druid```进行数据库连接池的配置

    <!--加载数据资源属性文件-->
    <context:property-placeholder location="classpath:db.properties"/>

	<!--使用druid数据源-->
    <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init">
        <!--注入连接属性-->
        <property name="driverClassName" value="${jdbc.driverClass}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${jdbc.user}"/>
        <property name="password" value="${jdbc.password}"/>
        <!--初始化连接池大小-->
        <property name="initialSize" value="5"></property>
        <!--设置最大连接数-->
        <property name="maxActive" value="10"></property>
        <!--设置等待时间-->
        <property name="maxWait" value="5000"></property>
        <!--属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有:监控统计用的filter:stat 日志用的filter:log4j 防御sql注入的filter:wall-->
        <property name="filters" value="stat"></property>
    </bean>
配置```sqlSessionFactory```：

    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!--注入数据源-->
        <property name="dataSource" ref="dataSource"></property>
		<!--配置mybatis全局配置文件文件-->
        <property name="configLocation" value="classpath:mybatis-config.xml"/>
        <!--注入mapper.xml-->
        <property name="mapperLocations">
            <array>
                <value>classpath:mapper/*/*Mapper.xml</value>
            </array>
        </property>
	</bean>

然后配置对```mapper包```多个文件的扫描：

    <!--扫描mapper接口-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!--注入mapper接口所在的包-->
        <property name="basePackage" value="com.morron.bus.mapper,com.morron.sys.mapper,com.morron.stat.mapper"></property>
        <!--注入sqlSessionFactory-->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"></property>
    </bean>

###1.2 service层的相关配置(```application-service.xml```)
该层主要是指定spring在创建容器时候需要扫描的包，因为从该层开始需要使用@service、@autowire等注解进行bean的注入.同时，该文件还需要对事务进行配置。

    <!--告知spring在创建容器时要扫描的包-->
    <context:component-scan base-package="com.morron.sys.service.impl"></context:component-scan>
    <context:component-scan base-package="com.morron.bus.service.impl"></context:component-scan>
    <context:component-scan base-package="com.morron.stat.service.impl"></context:component-scan>

    <!-- 1.声明事务管理器 -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"></property>
    </bean>

    <!-- 2.声明事务的传播特性 也就是通知 -->
    <tx:advice id="advise" transaction-manager="transactionManager">
        <tx:attributes>
            <!-- 以add开头的方法名需要事务 -->
            <tx:method name="add*" propagation="REQUIRED"/>
            <tx:method name="save*" propagation="REQUIRED"/>
            <tx:method name="update*" propagation="REQUIRED"/>
            <tx:method name="delete*" propagation="REQUIRED"/>
            <tx:method name="change*" propagation="REQUIRED"/>
            <tx:method name="reset*" propagation="REQUIRED"/>
            <tx:method name="get*" read-only="true"/>
            <tx:method name="load*" read-only="true"/>
            <tx:method name="*" read-only="true"/>
        </tx:attributes>
    </tx:advice>

    <!-- 3.进行AOP织入 -->
    <aop:config proxy-target-class="true" >
        <!-- 声明切面 -->
        <aop:pointcut expression="execution(* com.morron.sys.service.impl.*.*(..))" id="pc1"/>
        <aop:pointcut expression="execution(* com.morron.bus.service.impl.*.*(..))" id="pc2"/>
        <aop:pointcut expression="execution(* com.morron.stat.service.impl.*.*(..))" id="pc3"/>
        <!-- 织入 -->
        <aop:advisor advice-ref="advise" pointcut-ref="pc1"/>
        <aop:advisor advice-ref="advise" pointcut-ref="pc2"/>
        <aop:advisor advice-ref="advise" pointcut-ref="pc3"/>
    </aop:config>

###1.3 配置mybatis全局配置文件(```mybatis-config.xml```)

	<!--开启驼峰转换-->
    <settings>
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>

	<!--别名扫描器-->
    <typeAliases>
        <package name="com.morron.domain"/>
    </typeAliases>
    <plugins>
		<!--PageHelper插件配置-->
        <plugin interceptor="com.github.pagehelper.PageInterceptor">
            <!--分页参数合理化-->
            <property name="reasonable" value="true"/>
        </plugin>
    </plugins>
###1.4 springmvc的配置文件

配置对Controller注解的扫描：
    
	<!-- 扫描controller -->
	<context:component-scan base-package="com.morron.sys.controller"></context:component-scan>
	<context:component-scan base-package="com.morron.bus.controller"></context:component-scan>
	<context:component-scan base-package="com.morron.stat.controller"></context:component-scan>

需要特别注意对```<mvc:annotation-driven>```的配置，该配置可以开启一些高级功能。
> <mvc:annotation-driven>会自动注册RequestMappingHandlerMapping与RequestMappingHandlerAdapter两个Bean,这是Spring MVC为@Controller分发请求所必需的，并且提供了数据绑定支持，@NumberFormatannotation支持，@DateTimeFormat支持,@Valid支持读写XML的支持（JAXB）和读写JSON的支持（默认Jackson）等功能。

> 如果在web.xml中servlet-mapping的url-pattern设置的是/，而不是如.do。表示将所有的文件，包含静态资源文件都交给spring mvc处理。就需要用到<mvc:annotation-driven />了。如果不加，DispatcherServlet则无法区分请求是资源文件还是mvc的注解，而导致controller的请求报404错误。

	<!-- 配置映射器和适配器 -->
	<mvc:annotation-driven></mvc:annotation-driven>

配置前端识图解析器：

	<!-- 配置前端视图解析器 -->
	<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
		<!-- 注入前后缀 -->
		<property name="prefix" value="/WEB-INF/view/"></property>
		<property name="suffix" value=".jsp"></property>
	</bean>

配置文件上传数据解析器，在上传文件时需要配置。

	<!-- 配置spring MVC对文件上传的支持 -->
	<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
		<!-- 设置文件名的编码 -->
		<property name="defaultEncoding" value="utf-8"></property>
		<!-- 配置最上传文件的支持 20M -->
		<property name="maxUploadSize" value="20971520"></property>
	</bean>

	<!-- 配置静态文件放行 -->
	<mvc:default-servlet-handler />


###1.5 ```applicationContext.xml```的配置

	<!--告知spring在创建容器时要扫描的包-->
	<context:component-scan base-package="com.morron.sys,com.morron.bus"></context:component-scan>

在实际的项目开发中，我们往往会分为很多不同的包，如果遇见为不同的包都设置Spring配置文件的情况，都写在一个总的配置文件中，难免会造成配置文件内容臃肿，不易阅读的情况。

为了方便管理应用配置文件，推荐使用import来规划配置文件：

在Spring中，可以把配置文件分散到各个模块中，然后在总的配置文件中通过import元素引入这些配置文件：

1.默认情况下，使用相对路径来辅助查找配置文件

2.Spring还提供了两种前缀标记来辅助查找配置文件：

  （1）[classpath：]：表示从classpath开始寻找后面的资源文件

  （2）[file:]：表示使用文件系统的方式寻找后面的文件（文件的完整路径）<br>
在前面我们分别使用applicaiton-dao和application-servcie对数据持久层和service层进行配置，现在只需要将其整合到spring的全局配置文件applicationContext.xml中便可。

	<import resource="classpath:application-dao.xml"/>
	<import resource="classpath:application-service.xml"/>
	<import resource="classpath:application-task.xml"/>

###1.6 web.xml文件的配置

启动Spring的容器

	<context-param>
	   <param-name>contextConfigLocation</param-name>
	   <param-value>classpath:applicationContext.xml</param-value>
	</context-param>
	<listener>
	   <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

配置编码过滤器

    <filter>
       <filter-name>CharacterEncodeingFilter</filter-name>
       <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
       <init-param>
         <param-name>encoding</param-name>
       <param-value>UTF-8</param-value>
       </init-param>
    </filter>
    <filter-mapping>
       <filter-name>CharacterEncodeingFilter</filter-name>
       <servlet-name>dispatcherServlet</servlet-name>
    </filter-mapping>

配置springmvc前端控制器，拦截以.action为结尾的请求

    <servlet>
       <servlet-name>dispatcherServlet</servlet-name>
       <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
       <init-param>
         <param-name>contextConfigLocation</param-name>
         <param-value>classpath:springmvc.xml</param-value>
       </init-param>
       <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
       <servlet-name>dispatcherServlet</servlet-name>
       <url-pattern>*.action</url-pattern>
    </servlet-mapping>

配置druid的监控页面

    <servlet>
       <servlet-name>StatViewServlet</servlet-name>
       <servlet-class>com.alibaba.druid.support.http.StatViewServlet</servlet-class>
       <!--配置登陆名-->
       <init-param>
         <param-name>loginUsername</param-name>
         <param-value>root</param-value>
       </init-param>
       <!--配置密码-->
       <init-param>
         <param-name>loginPassword</param-name>
         <param-value>123456</param-value>
       </init-param>
       <!--设置白名单-->
       <init-param>
         <param-name>allow</param-name>
         <param-value></param-value>
       </init-param>
       <!--设置黑名单-->
       <init-param>
         <param-name>deny</param-name>
         <param-value></param-value>
       </init-param>
    </servlet>

    <servlet-mapping>
       <servlet-name>StatViewServlet</servlet-name>
       <url-pattern>/druid/*</url-pattern>
    </servlet-mapping>

    <filter>
       <filter-name>WebStatFilter</filter-name>
       <filter-class>com.alibaba.druid.support.http.WebStatFilter</filter-class>
       <!-- 注入过滤的样式文件 -->
       <init-param>
         <param-name>exclusions</param-name>
         <param-value>*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*</param-value>
       </init-param>
    </filter>

    <filter-mapping>
       <filter-name>WebStatFilter</filter-name>
       <servlet-name>StatViewServlet</servlet-name>
    </filter-mapping>

##2. 汽车租赁项目关键技术点回顾(以后端技术为主)

###2.1 sql语法部分
####2.1.1 mybatis中对模糊查询的实现
    <!--用户查询-->
    <select id="queryAllUser" resultMap="BaseResultMap" >
    select
    <include refid="Base_Column_List" />
    from sys_user
    <where>
      <if test="realname!=null and realname!=''">
        and realname like concat("%",#{realname},"%")
      </if>
      <if test="loginname!=null and loginname!=''">
        and loginname like concat("%",#{loginname},"%")
      </if>
      <if test="identity!=null and identity!=''">
        and identity like concat("%",#{identity},"%")
      </if>
      <if test="address!=null and address!=''">
        and address like concat("%",#{address},"%")
      </if>
      <if test="phone!=null and phone!=''">
        and phone like concat("%",#{phone},"%")
      </if>
      <if test="sex!=null">
        and sex=#{sex}
      </if>
      and type!=1
    </where>
    </select>

如上可知，通过```<where></where>```标签对所有条件进行判断，然后拼接sql语句，实现模糊查询。

####2.1.2 mybatis中批量查询/删除的实现

就以```delete```删除来说，通过将需要删除的对象的对应表的主键抑或是其他字段封装成数组传递到后台，再通过```<foreach></foreach>```对其进行遍历删除。语法可如下

    <delete id="deleteByBatch">
		delete from sys_log_login where id in 
		<foreach collection="ids"  item="id" separator="," open="(" close=")"> 
			#{id} 
		</foreach>
	</delete>

#####2.1.3 mybatis中具有多表关系的数据表的联合查询
就以汽车租赁系统来说，```sys_user```(用户表)和```sys_role```(角色表)具有一对多的关系，一个用户可以有多种角色,```sys_role```和```sys_menu```(权限表)也具有一对多的关系，一个角色可以有多种权限。将这三张表进行相互联系至少再需要两张表，一个是```sys_role_user```，将用户表和角色表联系，用户的uid和角色rid之间有一个对应关系。```sys_role_menu```将角色表和权限表进行联系，角色表的rid和权限表的mid有一个对应关系。<br>
若有需求如下，知道某用户的用户id，欲查出该用户所有的角色，可通过如下sql语句实现：

    select t1.* from sys_role t1 inner join sys_role_user t2 on(t1.roleid=t2.rid) whrer t2.uid = #{uid} 

若有需求如下，知道某用户的用户id，欲查出用户所拥有的权限，可通过如下sql语句实现：

	select t1.* from sys_menu t1 inner join sys_role_menu t2 inner join sys_role_user t3 on(t1.id=t2.mid and t2.rid=t3.rid) where t3.uid = userid


#####2.1.3 mybatis中对某个字段值出现的次数进行统计或对其对应的某个字段值进行计算

若有以下需求，查询用户表地区数量，可用如下sql语句实现：

	select address as name,count(*) as value from sys_customer GROUP BY adress

若欲查询某业务员的年度业绩，涉及对某字段的值进行相加，可用如下sql语法：
	
	select opername as name,sum(price) as value from bus_rent where DATE_FORMAT(createtime,"%Y")=#{value} GROUP BY opername
	

###2.2 数据封装部分
在本系统中应用比较多的封装类是TreeNode，ResultObj，DataGridView。

TreeNode.java类如下：
	
	public class TreeNode {
    private Integer id;
    @JsonProperty("parentId")
    private Integer pid;

    private String title;
    private String icon;
    private String href;
    private Boolean spread;
    private String target;
    private List<TreeNode> children = new ArrayList<>();

	    /**
	     * 复选树的必要属性   选中就是1
	     */
	    private String checkArr = "0";
	
	    /**
	     * 首页左边导航树的构造器
	     * @param id
	     * @param pid
	     * @param title
	     * @param icon
	     * @param href
	     * @param spread
	     * @param target
	     */
    public TreeNode(Integer id, Integer pid, String title, String icon, String href, Boolean spread, String target) {
        this.id = id;
        this.pid = pid;
        this.title = title;
        this.icon = icon;
        this.href = href;
        this.spread = spread;
        this.target = target;
    }
	
	    /**
	     * 给dTree的复选树使用
	     * @param id
	     * @param pid
	     * @param title
	     * @param spread
	     * @param checkArr
	     */
    public TreeNode(Integer id, Integer pid, String title, Boolean spread, String checkArr) {
        this.id = id;
        this.pid = pid;
        this.title = title;
        this.spread = spread;
        this.checkArr = checkArr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Boolean getSpread() {
        return spread;
    }

    public void setSpread(Boolean spread) {
        this.spread = spread;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public String getCheckArr() {
        return checkArr;
    }

    public void setCheckArr(String checkArr) {
        this.checkArr = checkArr;
    }

当我们查出所有menu信息后，将每一条menu的值赋给一个TreeNode对象，然后将所有TreeNode添加到list中。鉴于父menu下还有许多子menu，应此还应该将子menu和父menu建立映射关系。应此在TreeNode中有一个List<TreeNode> children属性，目的是为了封装父级Menu下的子menu。

ResultObj类是在执行完删除、更新、插入操作后，在不需要向前端返回数据库数据的情况下，封装一些提示信息的通用格式，如返回状态吗status、msg等信息，通过在前端进行展示，提示用户操作执行成功与否。

    public class ResultObj {

	    private Integer code;
	    private String msg;
	
	    /**
	     * 添加成功
	     */
	    public static final ResultObj ADD_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.ADD_SUCCESS);
	    /**
	     * 添加失败
	     */
	    public static final ResultObj ADD_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.ADD_ERROR);
	    /**
	     * 更新成功
	     */
	    public static final ResultObj UPDATE_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.UPDATE_SUCCESS);
	    /**
	     * 更新失败
	     */
	    public static final ResultObj UPDATE_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.UPDATE_ERROR);
	    /**
	     * 删除成功
	     */
	    public static final ResultObj DELETE_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.DELETE_SUCCESS);
	    /**
	     * 删除失败
	     */
	    public static final ResultObj DELETE_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.DELETE_ERROR);
	    /**
	     * 重置成功
	     */
	    public static final ResultObj RESET_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.RESET_SUCCESS);
	    /**
	     * 重置失败
	     */
	    public static final ResultObj RESET_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.RESET_ERROR);
	    /**
	     * 分配成功
	     */
	    public static final ResultObj DISPATCH_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.DISPATCH_SUCCESS);
	    /**
	     * 分配失败
	     */
	    public static final ResultObj DISPATCH_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.DISPATCH_ERROR);
	
	    /**
	     * 状态码0 成功
	     */
	    public static final ResultObj STATUS_TRUE = new ResultObj(SysConstast.CODE_SUCCESS);
	
	    /**
	     * 状态码-1 失败
	     */
	    public static final ResultObj STATUS_FALSE = new ResultObj(SysConstast.CODE_ERROR);
	
	
	    private ResultObj(Integer code, String msg) {
	        this.code = code;
	        this.msg = msg;
	    }
	
	    private ResultObj(Integer code) {
	        this.code = code;
	    }
	
	    public Integer getCode() {
	        return code;
	    }
	
	    public void setCode(Integer code) {
	        this.code = code;
	    }
	
	    public String getMsg() {
	        return msg;
	    }
	
	    public void setMsg(String msg) {
	        this.msg = msg;
	    }
	}


	public class ResultObj {

	    private Integer code;
	    private String msg;
	
	    /**
	     * 添加成功
	     */
	    public static final ResultObj ADD_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.ADD_SUCCESS);
	    /**
	     * 添加失败
	     */
	    public static final ResultObj ADD_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.ADD_ERROR);
	    /**
	     * 更新成功
	     */
	    public static final ResultObj UPDATE_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.UPDATE_SUCCESS);
	    /**
	     * 更新失败
	     */
	    public static final ResultObj UPDATE_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.UPDATE_ERROR);
	    /**
	     * 删除成功
	     */
	    public static final ResultObj DELETE_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.DELETE_SUCCESS);
	    /**
	     * 删除失败
	     */
	    public static final ResultObj DELETE_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.DELETE_ERROR);
	    /**
	     * 重置成功
	     */
	    public static final ResultObj RESET_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.RESET_SUCCESS);
	    /**
	     * 重置失败
	     */
	    public static final ResultObj RESET_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.RESET_ERROR);
	    /**
	     * 分配成功
	     */
	    public static final ResultObj DISPATCH_SUCCESS = new ResultObj(SysConstast.CODE_SUCCESS,SysConstast.DISPATCH_SUCCESS);
	    /**
	     * 分配失败
	     */
	    public static final ResultObj DISPATCH_ERROR = new ResultObj(SysConstast.CODE_ERROR,SysConstast.DISPATCH_ERROR);
	
	    /**
	     * 状态码0 成功
	     */
	    public static final ResultObj STATUS_TRUE = new ResultObj(SysConstast.CODE_SUCCESS);
	
	    /**
	     * 状态码-1 失败
	     */
	    public static final ResultObj STATUS_FALSE = new ResultObj(SysConstast.CODE_ERROR);
	
	
	    private ResultObj(Integer code, String msg) {
	        this.code = code;
	        this.msg = msg;
	    }
	
	    private ResultObj(Integer code) {
	        this.code = code;
	    }
	
	    public Integer getCode() {
	        return code;
	    }
	
	    public void setCode(Integer code) {
	        this.code = code;
	    }
	
	    public String getMsg() {
	        return msg;
	    }
	
	    public void setMsg(String msg) {
	        this.msg = msg;
	    }
	}

DataGridView类是本系统中封装后端信息的主要工具类。其属性除了code(状态码)、msg(信息)等前端常用的信息外，还有一个Object类型的data变量，该类主要用来接收各种方法，包括ArrayList<T>、Map<T>等常用的数据包装类型。
    public class DataGridView {
	    /**
	     * 封装LayUI数据表格的数据对象
	     */
	    private Integer code=0;
	    private String msg="";
	    private Long count;
	    private Object data;
	
	    public DataGridView() {
	    }
	
	    public DataGridView(Object data) {
	        super();
	        this.data = data;
	    }
	
	    public DataGridView(Long count, Object data) {
	        super();
	        this.count = count;
	        this.data = data;
	    }
	
	    public Integer getCode() {
	        return code;
	    }
	
	    public void setCode(Integer code) {
	        this.code = code;
	    }
	
	    public String getMsg() {
	        return msg;
	    }
	
	    public void setMsg(String msg) {
	        this.msg = msg;
	    }
	
	    public Long getCount() {
	        return count;
	    }
	
	    public void setCount(Long count) {
	        this.count = count;
	    }
	
	    public Object getData() {
	        return data;
	    }
	
	    public void setData(Object data) {
	        this.data = data;
	    }
    }

###2.3 图像处理部分

图片处理部分是一个文件上传和下载的问题。在本系统中，主要是车辆图片的设置和查看。文件上传和下载的工具类代码如下：

    public class AppFileUtils {
	
		/**
		 * 得到文件上传的路径
		 */
		public static String PATH="D:/upload/";
		static {
			InputStream stream = AppFileUtils.class.getClassLoader().getResourceAsStream("file.properties");
			Properties properties=new Properties();
			try {
				properties.load(stream);
				PATH=properties.getProperty("path");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		/**
		 * 文件下载
		 * @param response
		 * @param path
		 * @param oldName
		 * @return
		 */
		public static  ResponseEntity<Object> downloadFile(HttpServletResponse response, String path, String oldName) {
			//4.使用绝对路径+相对路径去找到文件对象
			File file=new File(AppFileUtils.PATH,path);
			//5.判断文件是否存在
			if(file.exists()) {
				try {
					try {
						//如果名字有中文 要处理编码
						oldName=URLEncoder.encode(oldName,"UTF-8");
					} catch (Exception e) {
						e.printStackTrace();
					}
					//把file转成一个bytes
					byte[] bytes=FileUtils.readFileToByteArray(file);
					HttpHeaders header=new HttpHeaders();
					//封装响应内容类型(APPLICATION_OCTET_STREAM 响应的内容不限定)
					header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
					//设置下载的文件的名称
					header.setContentDispositionFormData("attachment",oldName);
					//创建ResponseEntity对象
					ResponseEntity<Object> entity = new ResponseEntity<Object>(bytes,header,HttpStatus.CREATED);
					return entity;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}else {
				PrintWriter out;
				try {
					out = response.getWriter();
					out.write("文件不存在");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		}
	
		/**
		 * 根据相对路径删除硬盘上文件
		 * @param path
		 */
		public static void deleteFileUsePath(String path) {
			String realPath=PATH+path;
			//根据文件
			File file=new File(realPath);
			if(file.exists()) {
				file.delete();
			}
		}
	
		/**
		 * 更改文件名
		 * @param carimg
		 * @param suffix
		 */
	    public static String updateFileName(String carimg,String suffix) {
			//找到文件
			try{
				File file = new File(PATH,carimg);
				if (file.exists()){
					file.renameTo(new File(PATH,carimg.replace(suffix,"")));
					return carimg.replace(suffix,"");
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
	
		/**
		 * 根据路径删除图片
		 * @param carimg
		 */
		public static void removeFileByPath(String carimg) {
			//找到文件
			try{
				File file = new File(PATH,carimg);
				if (file.exists()){
					file.delete();
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


```FileController.java```代码如下：

	@Controller
	@RequestMapping("file")
	public class FileController {
	
		/**
		 * 添加
		 * @throws IOException
		 * @throws IllegalStateException
		 */
		@RequestMapping("uploadFile")
		@ResponseBody
		public DataGridView uploadFile(MultipartFile mf) throws IllegalStateException, IOException {
			// 文件上传的父目录
			String parentPath = AppFileUtils.PATH;
			// 得到当前日期作为文件夹名称
			String dirName = RandomUtils.getCurrentDateForString();
			// 构造文件夹对象
			File dirFile = new File(parentPath, dirName);
			if (!dirFile.exists()) {
				dirFile.mkdirs();// 创建文件夹
			}
			// 得到文件原名
			String oldName = mf.getOriginalFilename();
			// 根据文件原名得到新名
			String newName = RandomUtils.createFileNameUseTime(oldName,SysConstast.FILE_UPLOAD_TEMP);
			File dest = new File(dirFile, newName);
			mf.transferTo(dest);
			
			Map<String,Object> map=new HashMap<>();
			map.put("src", dirName+"/"+newName);
			return new DataGridView(map);
			
		}
	
		@RequestMapping("uploadImage")
		@ResponseBody
		public DataGridView uploadImage(MultipartFile file){
			return null;
		}
	
		/**
		 * 不下载只显示
		 */
		@RequestMapping("downloadShowFile")
		public ResponseEntity<Object> downloadShowFile(String path, HttpServletResponse response) {
			return AppFileUtils.downloadFile(response, path, "img");
		}
		
		/**
		 * 下载图片
		 * @param path
		 * @param response
		 * @return
		 */
		@RequestMapping("downloadFile")
		public ResponseEntity<Object> downloadFile(String path, HttpServletResponse response) {
			String oldName="";
			return AppFileUtils.downloadFile(response, path, oldName);	
		}

	}
    


</font>
