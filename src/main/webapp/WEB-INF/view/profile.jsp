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
<title>Profile</title>
<link href="/css/main.css" rel="stylesheet" type="text/css">
</head>

<body>
<!-- Main Body -->
<div class="container">
<!-- Navigation Bar -->
  <header>
    <a href="/">
	    <img src="/images/chatapp-logo.png" class="logo_header" alt="Incodable Logo">
	</a>
	<% if(request.getSession().getAttribute("user") != null){ %>
        <p class="greeting"> Hello, <%=request.getSession().getAttribute("user") %>!</p>
        <nav>
              <ul>
                <li><a href="/conversations"> Conversations </a></li>
                <% if(request.getSession().getAttribute("role").equals("admin"))  {%>
                    <li><a href="/admin"> Admin </a></li>
                <% } %>
                <li style="text-decoration: underline;"><div id="diamond" style="background-color: blue;"></div>
                <a href="/users/<%=request.getSession().getAttribute("user") %>"> Profile </a></li>
                <li><a href="/logout"> Log Out </a></li>
              </ul>
        </nav>
     <% } else { %>
     <nav>
        <ul>
            <li><a href="/">Back to Home</a></li>
        </ul>
     </nav>
     <% } %>
  </header>
    <div class="content2">
        <p class="heading" style="text-align:center;">[ <%= request.getAttribute("user") %>'s Profile Page ]</p>
    	  <div style="text-align: center;">
    			 <p style="display: inline-block; vertical-align: top; margin-right: 40px; font-size:20px; font-weight:lighter;"> About </p>
    		     <div class="textblock" style="display: inline-block;">
                    <p style="text-align:left;"><%=request.getAttribute("description")%></p>
    		     </div>
    	   </div>

        <% if(request.getSession().getAttribute("user").equals(request.getAttribute("user"))){ %>
              <br>
        	  <p class="heading"> Edit Your About Me (only you can see this)</p>
        	  <form action="" method="POST">
                  <textarea cols="100" id="description" name="description"></textarea>
                  <br>
                  <button type="submit">Submit</button>
              </form>
        <% } %>
    </div>
    <footer>
    	<h4 class="codeu">Google CodeU Chat App</h4>
    </footer>
</div>
</body>
</html>
