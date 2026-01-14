<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Le mie Candidature" scope="request"/>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/production.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3 mb-3">
    <div class="page-header">
        <h2 class="page-title"><i class="fas fa-file-contract"></i> Le mie Candidature</h2>
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary btn-sm">Cerca altro</a>
    </div>

    <div class="productions-card">
        <div class="table-responsive">
            <table class="production-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Casting</th>
                    <th>Data Invio</th>
                    <th>Stato</th>
                    <th>Feedback</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="app" items="${applications}">
                    <tr>
                        <td class="col-id">#${app.applicationID}</td>

                        <td>
                            <strong class="col-title">${castingTitles[app.castingID]}</strong>
                        </td>

                        <td>
                            <i class="far fa-calendar-alt"></i> ${app.sendingDate.toLocalDate()}
                        </td>

                        <td>
                                <span class="status-badge status-${app.status}">
                                        ${app.status.name().replace('_', ' ')}
                                </span>
                        </td>

                        <td>
                            <c:out value="${app.feedback}" default="-"/>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty applications}">
                    <tr><td colspan="5" class="empty-state text-center">Nessuna candidatura inviata.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>