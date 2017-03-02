package com.pdereg.timelogger.repository;

import com.pdereg.timelogger.Application;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.domain.Activity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class ActivityRepositoryIntTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ActivityRepository activityRepository;

    @Before
    public void setUp() {
        mongoTemplate.dropCollection(Activity.class);
    }

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(Activity.class);
    }

    @Test
    public void test() {

    }
}
