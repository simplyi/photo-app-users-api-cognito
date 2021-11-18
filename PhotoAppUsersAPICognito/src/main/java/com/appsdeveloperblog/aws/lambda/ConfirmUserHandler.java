package com.appsdeveloperblog.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.appsdeveloperblog.aws.lambda.service.CognitoUserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class ConfirmUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CognitoUserService cognitoUserService;
    private final String appClientId;
    private final String appClientSecret;

    public ConfirmUserHandler() {
        this.cognitoUserService = new CognitoUserService(System.getenv("AWS_REGION"));
        this.appClientId = Utils.decryptKey("MY_COGNITO_POOL_APP_CLIENT_ID");
        this.appClientSecret = Utils.decryptKey("MY_COGNITO_POOL_APP_CLIENT_SECRET");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        LambdaLogger logger = context.getLogger();

        try {
            String requestBodyJsonString = input.getBody();
            JsonObject requestBody = JsonParser.parseString(requestBodyJsonString).getAsJsonObject();
            String email = requestBody.get("email").getAsString();
            String confirmationCode = requestBody.get("code").getAsString();

            JsonObject confirmUserResult = cognitoUserService.confirmUserSignup(appClientId, appClientSecret, email, confirmationCode);
            response.withStatusCode(200);
            response.withBody(new Gson().toJson(confirmUserResult, JsonObject.class));
        } catch (AwsServiceException ex) {
            logger.log(ex.awsErrorDetails().errorMessage());
            ErrorResponse errorResponse = new ErrorResponse(ex.awsErrorDetails().errorMessage());
            String errorResponseJsonString = new Gson().toJson(errorResponse, ErrorResponse.class);
            response.withBody(errorResponseJsonString);
            response.withStatusCode(ex.awsErrorDetails().sdkHttpResponse().statusCode());
        } catch (Exception ex) {
            logger.log(ex.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
            String errorResponseJsonString = new GsonBuilder().serializeNulls().create().toJson(errorResponse, ErrorResponse.class);
            response.withBody(errorResponseJsonString);
            response.withStatusCode(500);
        }

        return response;
    }
}
