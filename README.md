# echatrest

## Documentation
All http requests have only a headers and look like that: http://localhost:8080/chat/get/by-participant?key=abc&id=0
### Authorization
Has an /authorization mapping.
|Command|Description|
|-------|-----------|
|/authorize?login={login}&password={password}| In response returns authorization entity which contains authorization key. With that key you must request almost all commands. If key passed to command that requires it wrong, returns http code 403|
|/logout?key={authorization key}| Returns only http code. Deletes authorization entity from database, so you cant use this key anymore. Keys are deleted automatically when 8 hours passed.|
### Profile
Has a /profile mapping.
|Command|Description|
|-------|-----------|
|/register?login={login}&password={password}| Adds profile entity to database. In response returns profile entity of created user.|
|/get/by-key?key={authorization key}| In response returns your profile entity.|
|/get/by-id?key={authorization key}&id={profile id}| In response returns all profiles that there are in database.|
|/get/by-name?key={authorization key}&name={search query}| Finds all profiles that contain a query in their login. In response returns list of search result.|
### Chat
Has a /chat mapping.
|Command|Description|
|-------|-----------|
|/create?key={authorization key}&name={chat name}&type{open or closed or empty}| Type parameter is not requied, in other words you can send http request without it. Adds chat entity to database. In response returns entity of your created chat.|
|/remove?key={authorization key}&id={chat id}| Removes chat if authorized user is chat admin and if it exists by received id. Returns http codes only.
|/get/all?key={authorization key}| Returns all chats in database that have open type.|
|/get/by-name?key={authorization key}&name={search query}| Returns open chats that contain query in their name.|
|/get/by-participant?key={authorization key}&id={profile id}| Returns open chats in which participant takes a part.|
|/remove/participant?key={authorization key}&chat-id={id}&login={participant login}&id={participant id}| Deletes participant if user that sent http request is chat admin. **Should only contain login OR id parameter.** Returns http codes only.|
|/add/admin??key={authorization key}&chat-id={id}&login={participant login}&id={participant id}| Marks profile if user that sent http request is chat admin. *Should only contain login OR id parameter.** Returns http codes only.|
|/remove/admin?key={authorization key}&chat-id={id}&login={participant login}&id={participant id}| Marks profile as regular participant if user that sent http request is chat admin *Should only contain login OR id parameter.** Returns http codes only.|
|/join?key={authorization key}&id={chat id}| Marks user's profile that sent http request as chat participant only if it is open. Returns http codes only.|
### Invite
Has a /invite mapping.
|Command|Description|
|-------|-----------|
|/invite?key={authorization key}&chat-id={id}&id={profile id}| Creates invite entity which contains chat and participant. Only chat admin can send invitation. Returns http codes only.|
|/get/all?key={authorization key}| Returns all invite entities that you received.|
|/accept?key={authorization key}&id={invitation id}| Removes invite from database and adds you to chat. Returns http codes only.|
|/decline?key={authorization key}&id={invitation id}| Removes invite from database. Returns http codes only.|
