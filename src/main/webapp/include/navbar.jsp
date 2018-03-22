<nav>
    <a id="navTitle" href="/">CodeU Chat App</a>
    <a href="/conversations">Conversations</a>
    <% if(request.getSession().getAttribute("user") != null){ %>
        <a>Hello <%= request.getSession().getAttribute("user") %>!</a>
    <% } else{ %>
        <a href="/login">Login</a>
        <!-- TODO (Azee): Add a link to /register -->
    <% } %>
    <a href="/about.jsp">About</a>
    <a href="/testdata">Load Test Data</a>
</nav>