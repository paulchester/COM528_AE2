<%-- 
    Document   : content
    Created on : Jan 4, 2020, 11:19:47 AM
    Author     : cgallen
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
// request set in controller
//    request.setAttribute("selectedPage","contact");
%>
<jsp:include page="header.jsp" />
<!-- Begin page content -->
<main role="main" class="container">
    <c:if test="${message != ''}">
        <div class="alert alert-success" role="alert">
            ${message}
        </div>
    </c:if>
    <c:if test="${errorMessage != ''}">
        <div class="alert alert-danger" role="alert">
            ${errorMessage}
        </div>
    </c:if>

    <div class="col-md-6 col-md-offset-3 col-sm-6 col-sm-offset-3">
        <p>Username must be unique and password must be at least 8 characters</p>
        <div class="panel panel-default" >
            <div class="panel-heading">
                <div class="panel-title text-center">Register</div>
            </div>     
            <div class="panel-body" >
                <form action="./register" method="post">
                    <input type="hidden" name="action" value="createNewAccount">
                    <div class="form-group">
                        <div class="input-group">
                            <span class="input-group-addon"><i class="glyphicon glyphicon-user"></i></span>
                            <input id="user" type="text" class="form-control" name="username" value="" placeholder="Username">                                        
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="input-group">
                            <span class="input-group-addon"><i class="glyphicon glyphicon-lock"></i></span>
                            <input id="password" type="password" class="form-control" name="password" placeholder="Password">
                        </div>                                                                  
                    </div>
                    <div class="form-group">
                        <div class="input-group">
                            <span class="input-group-addon"><i class="glyphicon glyphicon-lock"></i></span>
                            <input id="password2" type="password" class="form-control" name="password2" placeholder="Re Enter Password">
                        </div>                                                                  
                    </div>
                    <div class="form-group">
                        <div class="col-sm-12 controls">
                            <button type="submit" class="btn btn-primary pull-right"><i class="glyphicon glyphicon-check"></i> Register</button>                          
                        </div>
                    </div>
                </form>     
            </div>                     
        </div>
    </div>
</main>
<jsp:include page="footer.jsp" />
