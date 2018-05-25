<!doctype html>
<html lang="en-US">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Registration</title>
<link href="css/main.css" rel="stylesheet" type="text/css">
</head>

<body>
<!-- Main Body -->
<div class="container">
<!-- Navigation Bar -->
   <header>
   <a href="/">
    <img src="images/chatapp-logo.png" class="logo_header" alt="Incodable Logo">
    </a>
   <nav>
        <ul>
            <li><a href="/">Back to Home</a></li>
        </ul>
   </nav>
   </header>
   <!-- Content -->
   <div class="content">
        <p class="title">Register</p>
        <!-- Register Box -->
        <form action="/register" method="POST">
            <div class="fill_box">
                <label for="username">Username: </label>
                <input type="text" name="username" id="username">
                <br/>
                <label for="password">Password: </label>
                <input type="password" name="password" id="password">
                <br/><br/>
                <button type="submit">Submit</button>
     	    </div>
        </form>
        <% if(request.getAttribute("error") != null){ %>
                            <h4 style="color:red"><%= request.getAttribute("error") %></h4>
                        <% } %>
    </div>
    <footer>
	    <h4 class="codeu">Google CodeU Chat App</h4>
	</footer>
</div>
</body>
</html>