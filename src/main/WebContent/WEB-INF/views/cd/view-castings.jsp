<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="I miei Casting" scope="request"/>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/production.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3 mb-3">

    <div class="page-header">
        <h2 class="page-title"><i class="fas fa-bullhorn"></i> I miei Casting</h2>
        <a href="${pageContext.request.contextPath}/cd/create-casting" class="btn btn-primary">
            <i class="fas fa-plus"></i> Crea Annuncio
        </a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert-danger mb-2">${error}</div>
    </c:if>

    <div class="productions-card">
        <div class="table-responsive">
            <table class="production-table">
                <thead>
                <tr>
                    <th width="5%">ID</th>
                    <th width="25%">Titolo Annuncio</th>
                    <th width="20%">Produzione</th> <th width="15%">Categoria</th>
                    <th width="10%">Data Pubbl.</th>
                    <th width="10%">Scadenza</th>
                    <th width="15%" class="text-center">Azioni</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <%-- CASO: Lista Piena --%>
                    <c:when test="${not empty castings}">
                        <c:forEach var="c" items="${castings}">
                            <tr>
                                <td class="col-id">#${c.castingID}</td>

                                <td>
                                    <strong class="col-title">${c.title}</strong>
                                    <div style="font-size: 0.85rem; color: var(--color-text-secondary);">
                                        <i class="fas fa-map-marker-alt"></i> ${c.location}
                                    </div>
                                </td>

                                <td>
                                    <strong style="color: var(--color-primary-dark); font-size: 0.95rem;">
                                            ${productionTitles[c.castingID]}
                                    </strong>
                                </td>

                                <td>
                                    <span class="badge-type">${c.category}</span>
                                </td>

                                <td>
                                        ${c.publishDate.toLocalDate()}
                                </td>

                                <td>
                                    <jsp:useBean id="now" class="java.util.Date" />
                                    <c:set var="todayISO" value="<%= new java.sql.Date(new java.util.Date().getTime()).toString() %>" />

                                    <span class="${c.deadline.toLocalDate().toString() lt todayISO ? 'text-danger' : ''}">
                                        <i class="far fa-clock calendar-icon"></i>
                                        ${c.deadline.toLocalDate()}
                                    </span>
                                </td>

                                <td class="actions-cell">
                                    <div class="action-buttons">
                                        <a href="${pageContext.request.contextPath}/cd/applications?id=${c.castingID}"
                                           class="btn-circle btn-info" title="Vedi Candidature">
                                            <i class="fas fa-users"></i>
                                        </a>

                                        <a href="${pageContext.request.contextPath}/cd/edit-casting?id=${c.castingID}"
                                           class="btn-circle btn-edit" title="Modifica">
                                            <i class="fas fa-pen"></i>
                                        </a>

                                        <a href="${pageContext.request.contextPath}/cd/delete-casting?id=${c.castingID}"
                                           class="btn-circle btn-delete"
                                           title="Elimina"
                                           onclick="return confirm('Sei sicuro di voler eliminare il casting \'${c.title}\'?');">
                                            <i class="fas fa-trash"></i>
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>

                    <%-- CASO: Lista Vuota --%>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" class="empty-state">
                                <div class="empty-state-icon">
                                    <i class="fas fa-search-plus"></i>
                                </div>
                                <p class="empty-state-text">Non hai ancora pubblicato nessun casting.</p>
                                <a href="${pageContext.request.contextPath}/cd/create-casting" class="btn btn-sm btn-primary">
                                    Inizia ora
                                </a>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>

</main>

<jsp:include page="/WEB-INF/components/footer.jsp" />
<script src="${pageContext.request.contextPath}/scripts/mobile-menu.js"></script>

</body>
</html>