package com.appsdeveloperblog.aws.lambda.service;

import com.appsdeveloperblog.aws.lambda.shared.Constants;
import com.google.gson.JsonObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CognitoUserService {
    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    public CognitoUserService(String region) {
        this.cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
    }

    public CognitoUserService(CognitoIdentityProviderClient cognitoIdentityProviderClient) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
    }

    public JsonObject createUser(JsonObject user,
                                 String appClientId, String appClientSecret) {
        String email = user.get("email").getAsString();
        String password = user.get("password").getAsString();
        String userId = UUID.randomUUID().toString();
        String firstName = user.get("firstName").getAsString();
        String lastName = user.get("lastName").getAsString();

        AttributeType emailAttribute = AttributeType.builder()
                .name("email")
                .value(email)
                .build();

        AttributeType nameAttribute = AttributeType.builder()
                .name("name")
                .value(firstName + "  " + lastName)
                .build();

        AttributeType userIdAttribute = AttributeType.builder()
                .name("custom:userId")
                .value(userId)
                .build();

        List<AttributeType> attributes = new ArrayList<>();
        attributes.add(emailAttribute);
        attributes.add(nameAttribute);
        attributes.add(userIdAttribute);

        String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(email)
                .password(password)
                .userAttributes(attributes)
                .clientId(appClientId)
                .secretHash(generatedSecretHash)
                .build();

        SignUpResponse signupResponse = cognitoIdentityProviderClient.signUp(signUpRequest);
        JsonObject createUserResult = new JsonObject();
        createUserResult.addProperty(Constants.IS_SUCCESSFUL, signupResponse.sdkHttpResponse().isSuccessful());
        createUserResult.addProperty(Constants.STATUS_CODE, signupResponse.sdkHttpResponse().statusCode());
        createUserResult.addProperty(Constants.COGNITO_USER_ID, signupResponse.userSub());
        createUserResult.addProperty(Constants.IS_CONFIRMED, signupResponse.userConfirmed());

        return createUserResult;
    }

    public JsonObject confirmUserSignup(String appClientId,
                                  String appClientSecret,
                                  String email,
                                  String confirmationCode) {

        String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

        ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
                .secretHash(generatedSecretHash)
                .username(email)
                .confirmationCode(confirmationCode)
                .clientId(appClientId)
                .build();

        ConfirmSignUpResponse confirmSignUpResponse = cognitoIdentityProviderClient.confirmSignUp(confirmSignUpRequest);

        JsonObject confirmUserResponse = new JsonObject();
        confirmUserResponse.addProperty("isSuccessful", confirmSignUpResponse.sdkHttpResponse().isSuccessful());
        confirmUserResponse.addProperty("statusCode", confirmSignUpResponse.sdkHttpResponse().statusCode());
        return confirmUserResponse;

    }

    public JsonObject userLogin(JsonObject loginDetails, String appClientId, String appClientSecret) {

        String email = loginDetails.get("email").getAsString();
        String password = loginDetails.get("password").getAsString();
        String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

        Map<String, String> authParams = new HashMap<String, String>(){
            {
                put("USERNAME", email);
                put("PASSWORD",password);
                put("SECRET_HASH", generatedSecretHash);
            }
        };

        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .clientId(appClientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParams)
                .build();
        InitiateAuthResponse initiateAuthResponse =  cognitoIdentityProviderClient.initiateAuth(initiateAuthRequest);
        AuthenticationResultType authenticationResultType = initiateAuthResponse.authenticationResult();

        JsonObject loginUserResult = new JsonObject();
        loginUserResult.addProperty("isSuccessful", initiateAuthResponse.sdkHttpResponse().isSuccessful());
        loginUserResult.addProperty("statusCode", initiateAuthResponse.sdkHttpResponse().statusCode());
        loginUserResult.addProperty("idToken", authenticationResultType.idToken());
        loginUserResult.addProperty("accessToken", authenticationResultType.accessToken());
        loginUserResult.addProperty("refreshToken", authenticationResultType.refreshToken());

        return loginUserResult;

    }

    public JsonObject addUserToGroup(String groupName, String userName, String userPoolId) {
        AdminAddUserToGroupRequest adminAddUserToGroupRequest = AdminAddUserToGroupRequest.builder()
                .groupName(groupName)
                .username(userName)
                .userPoolId(userPoolId)
                .build();

        AdminAddUserToGroupResponse adminAddUserToGroupResponse =
                cognitoIdentityProviderClient.adminAddUserToGroup(adminAddUserToGroupRequest);

        JsonObject addUserToGroupResponse = new JsonObject();
        addUserToGroupResponse.addProperty("isSuccessful", adminAddUserToGroupResponse.sdkHttpResponse().isSuccessful());
        addUserToGroupResponse.addProperty("statusCode", adminAddUserToGroupResponse.sdkHttpResponse().statusCode());

        return addUserToGroupResponse;
    }

    public JsonObject getUser(String accessToken) {
        GetUserRequest getUserRequest = GetUserRequest.builder().accessToken(accessToken).build();
        GetUserResponse getUserResponse = cognitoIdentityProviderClient.getUser(getUserRequest);

        JsonObject getUserResult = new JsonObject();
        getUserResult.addProperty("isSuccessful", getUserResponse.sdkHttpResponse().isSuccessful());
        getUserResult.addProperty("statusCode", getUserResponse.sdkHttpResponse().statusCode());

        List<AttributeType> userAttributes = getUserResponse.userAttributes();
        JsonObject userDetails = new JsonObject();
        userAttributes.stream().forEach((attribute) -> {
            userDetails.addProperty(attribute.name(), attribute.value());
        });

        getUserResult.add("user", userDetails);

        return getUserResult;

    }

    public String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating ");
        }
    }

}
