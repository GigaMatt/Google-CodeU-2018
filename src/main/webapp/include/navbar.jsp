<nav>
    <a id="navTitle" href="/">CodeU Chat App</a>
    <a href="/conversations">Conversations</a>
    <% if(request.getSession().getAttribute("user") != null){ %>
        <a href="/users/<%= request.getSession().getAttribute("user") %>">Hello <%= request.getSession().getAttribute("user") %>!</a>
        <a href="/logout">Log Out</a>
        <%
      if(request.getSession().getAttribute("role").equals("admin"))  {
        %>
            <a href="/admin">Admin</a>
        <%
        } 
        %>
    <% } else{ %>
        <a href="/login">Login</a>
        <a href="/register">Register</a>
    <% } %>
    <a href="/about.jsp">About</a>
</nav>