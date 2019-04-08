<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<q0>Psst <a href="?secret_param=../WEB-INF/secret.jsp">click me</a> or <a href="?secret_param=../WEB-INF/web.xml">click me</a>!</q0>
<br/><br/>


<c:if test="${param.secret_param != null}">
<c:import url="${param.secret_param}" />
</c:if>