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
<!DOCTYPE html>
<html>
  <head>
   <title>Profile Page</title>
   <link rel="stylesheet" href="/css/main.css">
   <style>
     label {
       display: inline-block;
       width: 100px;
     }
   </style>
  </head>
  <body>

  <%@ include file="/include/navbar.jsp" %>
   <div id="container">
    <!-- prints the name of the user logged in, or an error message-->
    <% if(request.getSession().getAttribute("user") != null){ %>
        <h1><a><%= request.getSession().getAttribute("user") %>'s Profile Page</a></h1>
    <% } else{ %>
        <h1>You are not logged in!</h1>
    <% } %>
    <hr>
    <br>
     <!-- about section --> 
     <h3>About  <%= request.getSession().getAttribute("user") %> </h3>
     <!-- info from datastore -->
     <p> Test Data Here </p>
     <br>

     <h3> Edit your About Me (only you can see this)</h3>
     <form>
       <textarea cols="100">
         Test Data here
       </textarea>
       <br>
       <button>Submit</button>
     </form>
     <hr>

     <!-- messages section -->
     <h3><%= request.getSession().getAttribute("user") %>'s Sent Messages</h3>
   </div>
  </body>
</html>
