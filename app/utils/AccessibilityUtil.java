package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import exceptions.RequestException;
import models.BaseModel;
import models.Role;
import models.User;
import mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.cache.AsyncCacheApi;
import play.libs.Json;
import play.mvc.Http;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class AccessibilityUtil {

    @Inject
    private static IMongoDB mongoDB;

    @Inject
    private static AsyncCacheApi cacheApi;

    public static boolean readACL(User user, String resourceId,String collectionName,Class<? extends BaseModel> objectClass){
        try{
            parametersCheck(user,resourceId,collectionName,objectClass);
            List<String> roles = user.getRoles().stream().map(BaseModel::getId).map(ObjectId::toHexString).collect(Collectors.toList());
            roles.add(user.getId().toHexString());
            Object resource = objectClass.newInstance();
            List<? extends BaseModel> cachedList = getDataFromCache(collectionName,objectClass).join();
            Optional<? extends BaseModel> cachedResource = cachedList.stream().filter(x-> x.getId().toHexString().equals(resourceId)).findAny();
            resource = mongoDB.getMongoDatabase().getCollection(collectionName,objectClass)
                    .find(Filters.or(Filters.and(Filters.eq("_id",new ObjectId(resourceId)),Filters.in("readACL",roles)),
                            Filters.and(Filters.eq("_id",new ObjectId(resourceId)),(Filters.or(Filters.size("readACL",0),Filters.size("writeACL",0)))))).
                            first();
            return resource != null || !cachedResource.isPresent();
        }catch (RequestException e){
            throw new CompletionException(e);
        }catch (Exception e){
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
        }
    }

    public static <T> boolean writeACL(User user, String resourceId,String collectionName,Class<? extends BaseModel> objectClass){
        try{
            parametersCheck(user,resourceId,collectionName,objectClass);
            List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
            roles.add(user.getId().toHexString());
            Object resource = objectClass.newInstance();
            List<? extends BaseModel> cachedList = getDataFromCache(collectionName,objectClass).join();
            Optional<? extends BaseModel> cachedResource = cachedList.stream().filter(x-> x.getId().toHexString().equals(resourceId)).findAny();
            resource = mongoDB.getMongoDatabase().getCollection(collectionName,objectClass)
                    .find(Filters.or(Filters.and(Filters.eq("_id",new ObjectId(resourceId)),Filters.in("writeACL",roles)),
                            Filters.and(Filters.eq("_id",new ObjectId(resourceId)),Filters.or(Filters.size("readACL",0),Filters.size("writeACL",0))))).
                            first();
            return resource != null || !cachedResource.isPresent();
        }catch (RequestException e){
            throw new CompletionException(e);
        }catch (Exception e){
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
        }
    }

    public static <T> void parametersCheck(User user,String resourceId,String collectionName,Class<T> objectClass) throws RequestException {
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

    private static <T> CompletableFuture<List<T>> getDataFromCache(String collectionName,Class<T> objectClass){
        return CompletableFuture.supplyAsync(() -> {
            JsonNode cachedList = Json.toJson(CacheUtil.findDataInCache(cacheApi,collectionName));
            return ConverterUtil.jsonNodeToList(cachedList,objectClass);
        });
    }

}
