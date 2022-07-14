# hseb3-coursework-redstring
2015\. Study project (coursework at 3rd bachelor course). Plugin for NetBeans (Java app) with the implementation of interactive online synchronized whiteboard for collaboration.
## Structure
* `/RedStringAdminGUI` - interface for lobby administration and user permissions
* `/RedStringLibrary` - board and containers features 
* `/RedStringPlugin` - module for plugin integration
* `/RedStringServer` - server functions
* `/RedStringSingleGUI` - board interface
* `/RedStringSpamBot` - additional module for load testing
* `/SimpleChat` - additional module for chatting
## Features
1. 3 container types - text container, image container and file container
2. Container adding, naming, resizing, moving, changing order (front/back), changing background, clearing, deleting
3. Text editing with formatting
4. Loading content by drag-and-drop
5. Drawing in image containers
6. Scaling images
7. Pointing mark
8. Saving content to/from .brd file
9. Undo/redo feature
10. Online collaboration
11. Synchronized state between online editors (can be switched off), blocling containers when editing
12. Logging
13. Lobby administration (name, password, user permissions)
14. Chat
## Technology
Java (AWT, Swing, MigLayout, NIO, rslib)
## Screenshots
### Server creation
![hseb3-coursework-redstring-1](https://user-images.githubusercontent.com/6568251/179053581-bc9fb6e8-a9f4-473d-9a70-1d6344e67e1d.png)
### Server configuration
![hseb3-coursework-redstring-2](https://user-images.githubusercontent.com/6568251/179053601-346e0100-deac-444d-80c1-0f6454447c5a.png)
### Lobby management
![hseb3-coursework-redstring-3](https://user-images.githubusercontent.com/6568251/179053613-c9dc8cfa-8c58-4e62-9562-8e2b8f1ebc78.png)
### Board in NetBeans window
![hseb3-coursework-redstring-4](https://user-images.githubusercontent.com/6568251/179053620-1c98c5a6-3526-4337-87f7-c9cf85e6ea9d.png)
### Board containers
![hseb3-coursework-redstring-5](https://user-images.githubusercontent.com/6568251/179053627-c3938b61-ef51-42e2-8abe-92c979abb909.png)
