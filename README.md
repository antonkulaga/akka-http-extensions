akka-http-extensions
====================
Useful directives and utils for akka-http projects.

#Gettings started
-----------------

This git repository contains some useful classes and directives for akka-http.
akka.http.extensions are located in extensions subprojects. All other code is for previews.

The project contains 3 subprojects:: 
    * backend - scala bakcned
    * shared -  code (shared between backend and frontend)
    * frontend - ScalaJS frontend 
To run the project you must have SBT ( http://www.scala-sbt.org/ ) installed.

To run project::
    sbt //to opens sbt console
    re-start //Use this command **instead of** run to run the app
    Open localhost:1234 to see the result, it should reload whenever any sources are change

#Use in your own project
------------------------

Just add following dependencies
```sbt
resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases") //add resolver
libraryDependencies += "org.denigma" %%% "akka.http.extensions" % "0.0.1"
```

#What is inside
---------------

The project contains 4 subprojects::
    * extensions - the library itself, it is the artifact that is published 
    * preview/backend - scala backend example + tests
    * preview/controls -  some shared between backend and frontend of the preview example
    * preview/frontend - ScalaJS frontend for the example

The project was initiated because we had several akka-http projects and wanted to share
the most common features and patterns.
At the moment there are:

1) Custom directives for login and registration.
Login/registration is common for all apps. 
Here we provide some directives to make them easier as well encyption support
 (bcrypt is used to create password hashes).
Here how it looks like: 

```scala
class Registration(
                 login:(String,String)=>Future[LoginResult],
                 register:(String,String,String)=>Future[RegistrationResult],
                 getToken:String=>Future[String]
                 ) extends AuthDirectives
               with Directives
               with WithLoginRejections
               with WithRegistrationRejections
{



def routes: Route =
 pathPrefix("users") {
     pathPrefix("login") {
       handleRejections(loginRejectionHandlers){
         withLogin(login) { user=>
             withSession(user.username, getToken) { token =>
               setCookie(HttpCookie("token", content = token)) {
                 complete(s"The user ${user.username} was logged in")
             }
           }
         }
       }
     }~
   pathPrefix("register"){
     handleRejections(registerRejectionHandlers){
       withRegistration(register) {  user=>
         withSession(user.username, getToken) { token =>
           setCookie(HttpCookie("token", content = token)) {
             complete(s"The user ${user.username} has been registered")
           }
         }
       }
     }
   }
}
}
```

2 PJax support
---------------

PJax is a technique that makes it easier to load html pages without page reload.
The idea is that P-JAX header is added to all your ajax requests. 
So backend looks at the headers and loads whole template if there is not P-JAX header
or only a part of template if it was called for ajax. 
In Preview backend you can see P-JAX implementation for Twirl. Here is an example of test Router
with PJax directive:
```scala
  def test = pathPrefix("test"~Slash) { ctx=>
      pjax[Twirl](Html(s"<h1>${ctx.unmatchedPath}</h1>"),loadPage){h=>c=>
        val resp = HttpResponse(  entity = HttpEntity(MediaTypes.`text/html`, h.body  ))
        c.complete(resp)
      }(ctx)
    }
```
In the directive you add 

3 Session directive
-------------------
Basic session directive to store tokens.

4 Utils and stubs
------------------
* Some inmemory controllers for testing loging and registration
* BiMap - bidirectional map, it is missing in scala collections =(