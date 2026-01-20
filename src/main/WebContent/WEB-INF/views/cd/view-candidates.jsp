<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Candidati: ${casting.title}" scope="request"/>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/production.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/profile.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3 mb-3">

    <div class="page-header">
        <div>
            <h2 class="page-title"><i class="fas fa-users"></i> Candidati</h2>
            <p class="form-subtitle">Casting: <strong>${casting.title}</strong></p>
        </div>
        <a href="${pageContext.request.contextPath}/cd/view-castings" class="btn btn-secondary btn-sm">
            <i class="fas fa-arrow-left"></i> Torna alla lista casting
        </a>
    </div>

    <div class="productions-card">
        <div class="table-responsive">
            <table class="production-table">
                <thead>
                <tr>
                    <th>Candidato</th> <th>Contatti</th>  <th>Info</th>      <th>Stato</th>
                    <th class="text-center">Azioni</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty applications}">
                        <c:forEach var="app" items="${applications}">

                            <c:set var="u" value="${userDetails[app.performerID]}" />
                            <c:set var="p" value="${performerProfiles[app.performerID]}" />

                            <tr>
                                <td>
                                    <div class="candidate-profile-wrapper">
                                        <c:choose>
                                            <c:when test="${not empty p.profilePhoto}">
                                                <img src="${pageContext.request.contextPath}/uploads/${p.profilePhoto}"
                                                     alt="Foto"
                                                     class="candidate-avatar-img">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="avatar-circle candidate-avatar-placeholder">
                                                        ${u.firstName.charAt(0)}${u.lastName.charAt(0)}
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <div class="candidate-info-box">
                                            <strong class="candidate-name">${u.firstName} ${u.lastName}</strong>
                                            <span class="candidate-category">${p.category}</span>
                                        </div>
                                    </div>
                                </td>

                                <td>
                                    <a href="mailto:${u.email}" class="candidate-email-link">
                                            ${u.email}
                                    </a>
                                </td>

                                <td>
                                    <div class="candidate-date">
                                        <i class="far fa-clock"></i> ${app.sendingDate.toLocalDate()}
                                    </div>

                                    <c:choose>
                                        <c:when test="${not empty p.cvData}">
                                            <a href="${pageContext.request.contextPath}/download-cv?id=${p.performerID}"
                                               class="badge-type btn-download-cv">
                                                <i class="fas fa-file-pdf"></i> Scarica CV
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="no-cv-text">No CV</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                        <span class="status-badge status-${app.status}">
                                                ${app.status.name().replace('_', ' ')}
                                        </span>
                                </td>

                                <td class="text-center actions-cell">
                                    <div class="action-buttons">
                                        <a href="${pageContext.request.contextPath}/cd/performer-profile?id=${app.performerID}&appId=${app.applicationID}"
                                           class="btn-circle btn-info" title="Vedi Profilo">
                                            <i class="fas fa-eye"></i>
                                        </a>

                                        <form action="${pageContext.request.contextPath}/cd/update-status" method="post" class="action-form">
                                            <input type="hidden" name="appId" value="${app.applicationID}">
                                            <input type="hidden" name="status" value="Shortlist">
                                            <button type="submit" class="btn-circle btn-approve" title="Promuovi a Shortlist">
                                                <i class="fas fa-check"></i>
                                            </button>
                                        </form>

                                        <form action="${pageContext.request.contextPath}/cd/update-status" method="post" class="action-form">
                                            <input type="hidden" name="appId" value="${app.applicationID}">
                                            <input type="hidden" name="status" value="Rifiutata">
                                            <button type="submit" class="btn-circle btn-delete" title="Rifiuta" onclick="return confirm('Rifiutare questo candidato?')">
                                                <i class="fas fa-times"></i>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <tr>
                            <td colspan="5" class="empty-state">
                                <div class="empty-state-icon"><i class="fas fa-inbox"></i></div>
                                <p class="empty-state-text">Nessuna candidatura ricevuta per questo casting.</p>
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
</body>
</html>