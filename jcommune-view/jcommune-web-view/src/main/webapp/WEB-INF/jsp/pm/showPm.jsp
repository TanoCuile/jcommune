<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title><spring:message code="label.pm_title"/></title>
    <link href="${pageContext.request.contextPath}/css/main.css"
          type=text/css rel=stylesheet>
</head>
<body>
<div align="left">
    <jsp:include page="pmNavigationMenu.jsp"/>
    <div>
        <div style="float: left">
            <h3><c:out value="${pm.title}"/></h3>
        </div>
        <div style="float: right">
            <h3><joda:format value="${pm.creationDate}"
                             locale="${sessionScope['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE']}"
                             pattern="dd MMM yyyy HH:mm"/></h3>
        </div>
        <div style="clear:right;"></div>
        <table cellspacing=0 cellpadding=5 border="1">
            <tr>
                <td><spring:message code="label.sender"/></td>
                <td><c:out value="${pm.userFrom.username}"/></td>
            </tr>
            <tr>
                <td><spring:message code="label.recipient"/></td>
                <td><c:out value="${pm.userTo.username}"/></td>
            </tr>
            <tr>
                <td valign="top"><spring:message code="label.body"/></td>
                <td><c:out value="${pm.body}"/></td>
            </tr>
        </table>

        <table>
            <sec:authorize access="hasAnyRole('ROLE_USER','ROLE_ADMIN')">
                <tr>
                    <td>
                        <form:form
                                action="${pageContext.request.contextPath}/pm/${pm.id}/reply.html"
                                method="GET">
                            <input type="submit" value="<spring:message code="label.reply"/>"/>
                        </form:form>
                    </td>
                    <td>
                        <form:form
                                action="${pageContext.request.contextPath}/pm/${pm.id}/quote.html"
                                method="GET">
                            <input type="submit" value="<spring:message code="label.quote"/>"/>
                        </form:form>
                    </td>
                </tr>
            </sec:authorize>
        </table>


    </div>
</div>
</body>
</html>