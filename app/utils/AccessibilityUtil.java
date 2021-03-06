package utils;

import com.google.common.base.Strings;
import exceptions.RequestException;
import models.BaseModel;
import models.ChatRoom;
import models.User;
import mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.mvc.Http;
import types.UserACL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class AccessibilityUtil {

    @Inject
    private IMongoDB mongoDB;

    public <T> boolean withACL(User user, String resourceId, String collectionName, Class<T> objectClass, UserACL userACL) throws RequestException {
        try{
            parametersCheck(user,resourceId,collectionName,objectClass);
            List<String> roles = user.getRoles().stream().map(BaseModel::getId).map(ObjectId::toHexString).collect(Collectors.toList());
            roles.add(user.getId().toHexString());
            T resource = null;
            if(userACL.equals(UserACL.READ)) {
                resource = mongoDB.getMongoDatabase().getCollection(collectionName, objectClass)
                        .find(or(and(eq("_id", new ObjectId(resourceId)), in("readACL", roles)),
                                and(eq("_id", new ObjectId(resourceId)), (and(size("readACL", 0), size("writeACL", 0))))))
                        .first();
            }
            if(userACL.equals(UserACL.WRITE)){
                resource = mongoDB.getMongoDatabase().getCollection(collectionName,objectClass)
                        .find(or(and(eq("_id",new ObjectId(resourceId)),in("writeACL",roles)),
                                and(eq("_id",new ObjectId(resourceId)),and(size("readACL",0),size("writeACL",0))))).
                        first();
            }
                return resource != null;
        }catch (RequestException e){
            throw e;
        }catch (Exception e){
            throw new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable");
        }
    }

    public <T> void parametersCheck(User user,String resourceId,String collectionName,Class<T> objectClass) throws RequestException {
        if(user == null){
            throw new RequestException(Http.Status.BAD_REQUEST,"User cannot be empty");
        }
        if(!ObjectId.isValid(resourceId) || Strings.isNullOrEmpty(collectionName)){
            throw new RequestException(Http.Status.BAD_REQUEST,"Either resource Id or collection name is invalid.Please check your request");
        }
        if(objectClass == null){
            throw new RequestException(Http.Status.BAD_REQUEST,"Class cannot be empty!");
        }
    }


    public boolean isGroupAdmin(User user,String roomId) throws RequestException {
        try{
            if(Strings.isNullOrEmpty(roomId) || user == null){
                throw new RequestException(Http.Status.BAD_REQUEST,"Either user or roomId was null.Please try again!");
            }
            ChatRoom chatRoom = mongoDB.getMongoDatabase().getCollection("Channels",ChatRoom.class)
                    .find(and(eq("_id",new ObjectId(roomId)),eq("groupAdmin",user.getId())))
                    .first();
            return chatRoom != null;
        }catch (RequestException e){
            throw e;
        }catch (Exception e){
            throw new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable");
        }
    }
}
