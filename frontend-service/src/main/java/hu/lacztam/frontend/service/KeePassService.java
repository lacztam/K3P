package hu.lacztam.frontend.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class KeePassService {

    private final String HASH_KEY = "KeePassFile";

//    @Resource(name="redisTemplate")          // 'redisTemplate' is defined as a Bean in AppConfig.java
//    private HashOperations<String, String, KeePassModel> hashOperations;
    private RedisTemplate redisTemplate;

    public FrontEndKeePassModel saveKeePassModel(FrontEndKeePassModel frontEndKeePassModel) {
        //creates one record in Redis DB if record with that Id is not present
//        hashOperations.putIfAbsent(hashReference, keePassModel.getId(), keePassModel);
        redisTemplate.opsForHash().put(HASH_KEY, frontEndKeePassModel.getId(), frontEndKeePassModel);
        return frontEndKeePassModel;
    }

//    public void saveAllEmployees(Map<String, RedisModel> map) {
//        hashOperations.putAll(hashReference, map);
//    }

    public FrontEndKeePassModel getOneKeePassFile(String id) {
//        return hashOperations.get(HASH_KEY, id);
        return (FrontEndKeePassModel) redisTemplate.opsForHash().get(HASH_KEY, id);
    }

    public int createHashForOriginalKeePassFile(FrontEndKeePassModel frontEndKeePassModel) {
        return new HashCodeBuilder(17, 31)
                .append(frontEndKeePassModel.getKeePassFile())
                .toHashCode();
    }

    public int createHashForCurrentKeePassFile(FrontEndKeePassModel frontEndKeePassModel) {
        return new HashCodeBuilder(17, 31)
                .append(frontEndKeePassModel.getKeePassFile())
                .toHashCode();
    }

    public boolean isHashCodeEquals(Integer originalHashCodeOfKeePassFile, Integer currentHashCodeOfKeePassFile){
        if(originalHashCodeOfKeePassFile != null && currentHashCodeOfKeePassFile != null)
            return (originalHashCodeOfKeePassFile == currentHashCodeOfKeePassFile);
        else
            throw new NullPointerException("Original KeePass file hash code or current KeePass file hash code is missing.");
    }


//    public void updateEmployee(RedisModel redisModel) {
//        hashOperations.put(hashReference, redisModel.getId(), redisModel);
//    }

//    public Map<String, RedisModel> getAllEmployees() {
//        return hashOperations.entries(hashReference);
//    }

//    public void deleteEmployee(String id) {
//        hashOperations.delete(hashReference, id);
//    }

}
