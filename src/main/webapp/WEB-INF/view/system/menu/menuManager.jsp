<%--
  Created by IntelliJ IDEA.
  User: Morron
  Date: 2019/9/30
  Time: 22:57
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>菜单管理</title>
</head>

<%--如果使用frameset 包含页面 主页面不能有body--%>
<frameset cols="230,*" border="1">
    <frame src="${morron}/sys/toMenuLeft.action" name="left">
    <frame src="${morron}/sys/toMenuRight.action" name="right">
</frameset>


</html>
