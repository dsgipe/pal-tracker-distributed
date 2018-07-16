package io.pivotal.pal.tracker.allocations;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.ConcurrentMap;

public class ProjectClient {
    private Logger logger;

    private final RestOperations restOperations;
    private final String registrationServerEndpoint;

    private ConcurrentMap<Long, ProjectInfo> map;
    private RedissonClient redisson;
    private void ConfigureRedis(){
        redisson = Redisson.create();
        map=redisson.getMap("someMap");
        }

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {

        ConfigureRedis();

        logger = LoggerFactory.getLogger(getClass());
        this.restOperations= restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
    }

    @HystrixCommand(fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo projectInfo = restOperations.getForObject
                (registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);

        map.put(projectId,projectInfo);
        return projectInfo;
    }

    public ProjectInfo getProjectFromCache(long projectId) {
        logger.info("Getting project with id {} from cache", projectId);
        return map.get(projectId);
    }

}
