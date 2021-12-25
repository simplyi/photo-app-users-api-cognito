package com.appsdeveloperblog.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.appsdeveloperblog.aws.lambda.service.CognitoUserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateUserHandlerTest {

    @Mock
    CognitoUserService cognitoUserService;

    @Mock
    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent;

    @Mock
    Context context;

    @Mock
    LambdaLogger lambdaLoggerMock;

    @InjectMocks
    CreateUserHandler handler;

    @BeforeEach
    public void runBeforeEachTestMethod() {
        System.out.println("Executing @BeforeEach method");
    }

    @AfterEach
    public void runAfterEachTestMethod() {
       System.out.println("Executing @AfterEach method");
    }

    @BeforeAll
    public static void runBeforeAllTestMethods()  {
        System.out.println("Executing @BeforeAll method");
    }

    @AfterAll
    public static void runAfterAllTestMethods() {
        System.out.println("Executing @AfterAll method");
    }

    @Test
    public void testHandleRequest_whenValidDetailsProvided_returnsSuccessfulResponse() {
        // Arrange or Given
        JsonObject userDetails = new JsonObject();
        userDetails.addProperty("firstName","Sergey");
        userDetails.addProperty("lastName","Kargopolov");
        userDetails.addProperty("email","sergey.kargopolov@gmail.com");
        userDetails.addProperty("password","12345678");

        String userDetailsJsonString = new Gson().toJson(userDetails, JsonObject.class);

        when(apiGatewayProxyRequestEvent.getBody()).thenReturn(userDetailsJsonString);

        when(context.getLogger()).thenReturn(lambdaLoggerMock);

        JsonObject createUserResult = new JsonObject();
        createUserResult.addProperty("isSuccessful", true);
        createUserResult.addProperty("statusCode", 200);
        createUserResult.addProperty("cognitoUserId", UUID.randomUUID().toString());
        createUserResult.addProperty("isConfirmed", false);
        when(cognitoUserService.createUser(any(), any(), any())).thenReturn(createUserResult);

        // Act or When

        // Assert or Then
        verify(lambdaLoggerMock, times(1)).log(anyString());

    }
}
