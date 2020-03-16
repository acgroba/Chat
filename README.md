Práctica de Middleware Orientado a Mensajes 
===================

Objetivo
-------------
En esta primera práctica de la asignatura se va a desarrollar un chat de tipo IRC en el que los usuarios podrán acceder a distintas salas y comunicarse entre sí haciendo uso de JMS.

Planificación
-------------
Para el desarrollo de la práctica se ha seguido una metodología ágil que finalmente (tras las modificaciones durante el trascurso del proyecto) ha sido la siguiente:

#### Sprint 1 (16/10/2017-23/10/2017)

 - **Definir Planificación** (Workitem)
 

#### Sprint 2 (24/10/2017-1/11/2017)

 - **Implementación Gestor de Salas** (feature):
 
-Añadir Salas Chat (user-story)

-Eliminar Salas Chat(user-story)

-Notificar Modificacions de Salas (user-story)

 - **Creación de Salas por Usuario** (feature):
 
-Añadir Sala por Usuario (user-story)

-Borrar Sala por Usuario(user-story)


#### Sprint 3 (2/11/2017-8/11/2017)

 - **Interacción Usuarios con Salas de Chat** (feature):
 
-Acceso Usuario a Sala de Chat(user-story)

-Abanadono de Usuario Sala de Chat(user-story)

#### Sprint 4 (9/11/2017-15/11/2017)

 - **Gestión de Usuarios** (feature):
 
-Autenticar(user-story)

-Registrar (user-story)

#### Sprint 5 (16/11/2017-29/11/2017)

 - **Notificación a Usuarios** (feature):
 
-Enviar Notificación a Usuario (user-story)

-Recibir Notificación de Usuario (user-story)

#### Sprint 6 (08/01/2018-15/01/2018)
 - **Memoria** (Workitem)


Arquitectura
-------------
![Diagrama de la arquitectura](/img/arq.jpg)

En la arquitectura de esta práctica distinguiremos tres  grandes segmentos: los usuarios  (que podrán comunicarse entre sí dentro de las salas de chat), un servidor de usuarios para el registro y autenticación de los mismos y un gestor de salas que adoptará el papel de administrador de las salas de chat y de notificador de los cambios en las mismas. Además, el gestor de salas contará con un MessageSender para el envío de mensajes de ADMIN a todos los usuarios. Dicho MessageSender también será utilizado por una clase Room, que materializará las salas y  será la encargada de manterse a la escucha de menciones (utilizando su MentionListener), para notificar al usuario mencionado. A continuación explicaremos más en detalle todas las clases:

#### User

La clase User será la encargada de materializar a los usuarios. Cada usuario mantendrá un Queue "queueAuth" para realizar las peticiones de login o registro al UserServer. Del mismo recibirá las respuestás a través de una TemporaryQueue "tempDest" (comunicación cliente-servidor) confirmando o negando las operaciones anteriores. A su vez, mantendrá una Queue "userRequests" en la que solo escuchará el RoomsManager(por lo que los mensajes le llegarán todos a él) para enviarle al gestor de salas información sobre qué operación se quiere realizar (crear sala, acceder sala, borrar sala...), y también un Topic "admin" por el que recibirá los mensajes broadcast de administración que han de llegar a todos os usuarios (creación de salas, borrado...)  y las menciones que se hayan dado en salas aunque no esté conectado a ellas.  Cuando se conecte a una sala se creará también un topic con el nombre de la sala al que estará suscrito de forma "Durable" para que, si vuelve a una sala, reciba los mensajes enviados en su ausencia. Dispone de los siguientes métodos:
 - **login**: realizar inicio de sesión.
 - **register**: registrar un nuevo usuario.
 - **connectToRoom**: conectarse a una sala determinada para comenzar a recibir sus mensajes.
 - **sendToRoom**: enviar mensaje indicado a la sala a la que se está conectado.
 - **onMessage**: manejo de los mensajes recibidos. Los MapMessages corresponderán a las menciones mientras que los TextMessages corresponderán a mensajes del Gestor de Salas o de la conversación de la sala.
 - **createRandomString**: para la creación del correlationId que permita saber a que mensaje corresponde la respuesta del servidor de usuarios.


#### UsersServer
Clase que encapsula el servidor de autenticación y registro de users. Se encuantra conectado a los usuarios mediante una Queue "queueAuth" por la que recibe las peticiones de login o registro y una TemporaryQueue creada en el momento de la operación. Mantiene un HashMap con los nombres de usuario y contraseñas de los usuarios registrados.

 - **login**: realiza el  inicio de sesión si el usuario existe y su contraseña coincide con la indicada.
 - **register**: registrar un nuevo usuario en caso de que no exista ya.
 - **onMessage**: gestionar las solicitudes que le llegan a traves de "auth". El código 1 corresponde a registros y el 2 a inicios de sesión.

#### RoomsManager

Esta clase materializará el "administrador" del chat, encargado último de la creación y elminación de salas así como de enviar las notificaciones relevantes para todos los usuarios. Es el único a la escucha en la Queue "userRequests" por la que los distintos usuarios le envían peticiones para que realice operaciones de gestión de salas. Cuenta con los siguientes métodos:

 - **getSalasDisponibles**: por él crea y envía una lista con cuales son las salas existentes.

 - **onMessage**: gestionar las solicitudes que le llegan a traves de "userRequests". Los MapMessages corresponden a las operaciones de gestión de salas. Estas se les pasa a una clase auxiliar ManageRoom, que es la que mantiene un HashMap con las salas disponibles y sus propietarios, que será la encargada de crear o borrar las salas. Los TextMessages corresponden a peticiones del usuario para que se le envíe una lista de las salas disponibles en ese momento.
 
Para el envío de mensajes de administración (notificar creacion de salas, salas disponibles, etc) utiliza la clase auxiliar **MessageSender** que es la que realmente envía (y solo lo hace ella) mensajes a través del topic "admin" cuando se invoca su método *sendAdminMsg* . Dicha clase también cuenta con otro método *sendMention* para notificar (a través del mismo topic) a un usuario concreto (solo lo imprime aquel cuyo usuario corresponde con el usuario mencionado) que se le ha mencionado en alguna sala. La funcionalidad de las menciones provoca que necesitemos de otro par de clases auxiliares que estarán escuchando en los Topic de cada sala  si se produce una mención. Estás clases serán **Room** (materializa las salas) que a su vez tiene un **MentionListener** analizando mediante una expresión regular si se producen menciones.


Guía de uso
-------------

Previo a la ejecución se han de añadir a todos los proyectos las librerias imq.jar y jms.jar. A continuación, se ha de ejecutar el proyecto RoomsManager y UsersServer y  después se ejecutarán los Users. Inicialmente se nos preguntará si queremos registrarnos (1) o iniciar sesión si ya tenemos una cuenta (2). Primero nos registraremos y a continuación iniciaremos sesión. 

![Registro](/img/registro.jpg)

![Login](/img/login.jpg)

Tras iniciar sesión (o registrarnos correctamente) se nos mostrará un menú principal con tres opciones: acceder una sala (0), crear una sala (1) o eliminarla (2). Primero la crearemos. El administrador nos notificará la lista de salas disponibles (iniciamente "no rooms) para que no repitamos nombres. Nos pedirá el nombre de la sala y finalmente habremos creado una nueva sala de chat a la que podremos acceder.

![Crear Sala](/img/crearSala.jpg)

Ahora ya podremos acceder a una sala (0) indicando su nombre, donde podremos entablar conversación con otros usuarios.

![Chat](/img/chat.jpg)

Cuando queramos abandonar la conversación debemos escribir la palabra "exit" y se nos devolverá al menú inicial.

![Exit](/img/exit.jpg)

Aunque estemos ausentes en una sala, si un usuario nos menciona utilizando "@" y nuestro nombre, recibiremos el mensaje de todas formas.

![Mención](/img/mencion.jpg)

Si regresamos a una sala en la que hemos estado, recibiremos todos los mensajes que se han enviado en nuestra ausencia.

![Suscripción Durable](/img/durable.jpg)

Si lo deseamos, desde el menú principal podremos también borrar una sala siempre y cuando hayamos sido su creador.

![Borrado](/img/borrar.jpg)



