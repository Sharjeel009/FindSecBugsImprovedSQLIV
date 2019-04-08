<q0>Psst <a href="?secret_param=../WEB-INF/secret.jsp">click me</a> or <a href="?secret_param=../WEB-INF/web.xml">click me</a>!</q0>
<br/><br/>

<%@include file="${param.secret_param}.jsp"%> <!-- Safe will be evaluate as literal -->