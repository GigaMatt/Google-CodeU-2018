<%--
  Copyright 2017 Google Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<!doctype html>
<html lang="en-US">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Log In</title>
<link href="css/main.css" rel="stylesheet" type="text/css">
</head>

<body>
	<!-- Main Body -->
<div class="container">
    <!-- Navigation Bar -->
    <header>
	    <img src="images/chatapp-logo.png" class="logo_header" alt="Incodable Logo">
        <nav>
            <ul>
                <li><a href="/">Back to Home</a></li>
            </ul>
        </nav>
    </header>
	<!-- Content -->
    <div class="content">
        <p class="title">Log In</p>
	    <!-- Log In Box -->
        <form action="/login" method="POST">
            <div class="fill_box">
       		    <label for="username">Username: </label>
                <input type="text" name="username" id="username">
                <br/>
                <label for="password">Password: </label>
                <input type="password" name="password" id="password">
                <br/><br/>
                <button type="submit">Login</button>
                <% if(request.getAttribute("error") != null){ %>
                    <h4 style="color:red"><%= request.getAttribute("error") %></h4>
                <% } %>
       	    </div>
        </form>
    </div>
    <footer>
	<h4 class="codeu">Google CodeU Chat App</h4>
	</footer>
</div>
</body>
</html>
