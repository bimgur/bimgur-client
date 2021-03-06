package ch.fhnw.ima.bimgur.activiti.service;

import ch.fhnw.ima.bimgur.activiti.IntegrationTest;
import ch.fhnw.ima.bimgur.activiti.TestUtils;
import ch.fhnw.ima.bimgur.activiti.model.*;
import io.reactivex.Observable;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

@IntegrationTest
public class FormDataServiceTest {

    private static FormDataService formDataService;
    private static final ProcessEngine PROCESS_ENGINE = TestUtils.processEngine();
    private org.activiti.engine.identity.User firstUserByProcessEngine;
    private org.activiti.engine.task.Task firstTaskByProcessEngine;

    @BeforeAll
    static void beforeAll() {
        formDataService = TestUtils.client().getFormDataService();
        TestUtils.resetDeployments();
        TestUtils.loadAndDeployTestDeployments(
                Collections.singletonList("demo-japanese-numbers.bpmn20.xml"));
    }

    @BeforeEach
    void beforeEach() {
        TestUtils.deleteAllProcessInstances();
        TestUtils.deleteAllTasks();

        PROCESS_ENGINE.getRuntimeService()
                .startProcessInstanceByKey("bimgur-demo-japanese-numbers", Collections.singletonMap("iteration", 0));

        this.firstUserByProcessEngine = PROCESS_ENGINE.getIdentityService().createUserQuery().list().get(0);
        this.firstTaskByProcessEngine = PROCESS_ENGINE.getTaskService().createTaskQuery().list().get(0);

    }

    @Test
    void getFormDataListByProcessDefinitionId() {
        PROCESS_ENGINE.getTaskService().claim(
                firstTaskByProcessEngine.getId(),
                firstUserByProcessEngine.getId()
        );

        formDataService.getStartFormData(new ProcessDefinitionId(firstTaskByProcessEngine.getProcessDefinitionId()))
                .flatMap(formData -> Observable.fromIterable(formData.getFormProperties()))
                .test()
                .assertComplete();
    }

    @Test
    void getFormDataFormPropertyName() {
        Task task = PROCESS_ENGINE.getTaskService().createTaskQuery().list().get(0);
        User user = PROCESS_ENGINE.getIdentityService().createUserQuery().list().get(0);
        PROCESS_ENGINE.getTaskService().claim(
                task.getId(),
                user.getId()
        );

        formDataService.getTaskFormData(new TaskId(task.getId()))
                .flatMap(formData -> Observable.fromIterable(formData.getFormProperties()))
                .map(FormProperty::getName)
                .test().assertResult("FormProperty1", "FormProperty2");
    }

    @Test
    void submitTaskFormData() {
        Task infoTask = PROCESS_ENGINE.getTaskService().createTaskQuery().list().get(0);
        User user = PROCESS_ENGINE.getIdentityService().createUserQuery().list().get(0);

        PROCESS_ENGINE.getTaskService().claim(infoTask.getId(), user.getId());
        PROCESS_ENGINE.getTaskService().complete(infoTask.getId());

        Task analysisTask = PROCESS_ENGINE.getTaskService().createTaskQuery().list().get(0);
        PROCESS_ENGINE.getTaskService().claim(analysisTask.getId(), user.getId());

        List<Tuple2<FormPropertyId, String>> properties = List.of(
                Tuple.of(new FormPropertyId("numberA"), "42")
        );

        formDataService.submitTaskFormData(new TaskId(analysisTask.getId()), properties)
                .test()
                .assertComplete();
    }

    @Test
    void submitStartFormData() {
        Task infoTask = PROCESS_ENGINE.getTaskService().createTaskQuery().list().get(0);
        User user = PROCESS_ENGINE.getIdentityService().createUserQuery().list().get(0);

        PROCESS_ENGINE.getTaskService().claim(infoTask.getId(), user.getId());
        PROCESS_ENGINE.getTaskService().complete(infoTask.getId());

        Task analysisTask = PROCESS_ENGINE.getTaskService().createTaskQuery().list().get(0);
        PROCESS_ENGINE.getTaskService().claim(analysisTask.getId(), user.getId());

        List<Tuple2<FormPropertyId, String>> properties = List.of(
                Tuple.of(new FormPropertyId("numberA"), "42")
        );

        formDataService.submitStartFormData(new ProcessDefinitionId(analysisTask.getProcessDefinitionId()), properties)
                .test()
                .assertComplete();
    }


}