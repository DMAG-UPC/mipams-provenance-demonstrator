package org.mipams.fake_media.demo.entities.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mipams.fake_media.demo.entities.requests.FakeMediaRequest;
import org.mipams.fake_media.demo.entities.responses.FakeMediaResponse;
import org.mipams.fake_media.entities.assertions.Assertion;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.fake_media.entities.assertions.ActionAssertion;
import org.mipams.fake_media.entities.assertions.BindingAssertion;
import org.mipams.fake_media.entities.assertions.ExifMetadataAssertion;
import org.mipams.fake_media.entities.assertions.IngredientAssertion;
import org.mipams.fake_media.entities.assertions.RedactableAssertion;
import org.mipams.fake_media.entities.assertions.ThumbnailAssertion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FakeMediaUtils {
    public static String generateJsonResponseFromString(Object response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public static String prepareResponse(List<FakeMediaResponse> responseList) throws MipamsException {

        ObjectMapper mapper = new ObjectMapper();
        String response;
        try {
            response = mapper.writeValueAsString(responseList);
        } catch (JsonProcessingException e) {
            throw new MipamsException(e.getMessage());
        }

        return response;
    }

    public static FakeMediaRequest populateRequest(JsonNode requestNode) throws MipamsException {

        if (requestNode == null) {
            throw new MipamsException("No parameters specified in request");
        }

        FakeMediaRequest request = new FakeMediaRequest();

        if (requestNode.get("assetUrl") == null) {
            throw new MipamsException("assetUrl param cannot be empty");
        }

        if (requestNode.get("modifiedAssetUrl") == null) {
            throw new MipamsException("modifiedAssetUrl param cannot be empty");
        }

        request.setAssetUrl(requestNode.get("assetUrl").asText());

        request.setModifiedAssetUrl(requestNode.get("modifiedAssetUrl").asText());

        JsonNode assertionListNode = requestNode.get("assertionList");
        populateAssertionList(request, assertionListNode);

        JsonNode encryptionAssertionListNode = requestNode.get("encryptionAssertionList");
        request.setEncryptionAssertionList(getRedactableAssertionList(encryptionAssertionListNode));

        JsonNode encryptionWithAccessRulesAssertionListNode = requestNode.get("encryptionWithAccessRulesAssertionList");
        request.setEncryptionWithAccessRulesAssertionList(
                getRedactableAssertionList(encryptionWithAccessRulesAssertionListNode));

        JsonNode redactedAssertionUriListNode = requestNode.get("redactedAssertionUriList");
        request.setRedactedAssertionUriList(populateStringList(request, redactedAssertionUriListNode));

        JsonNode componentIngredientUriListNode = requestNode.get("componentIngredientUriList");
        request.setComponentIngredientUriList(populateStringList(request, componentIngredientUriListNode));

        if (requestNode.get("outputAssetName") == null) {
            throw new MipamsException("assetUrl param cannot be empty");
        }
        String outputAssetName = requestNode.get("outputAssetName").asText();
        request.setOutputAssetName(outputAssetName);
        return request;
    }

    private static void populateAssertionList(FakeMediaRequest request, JsonNode assertionListNode)
            throws MipamsException {

        List<Assertion> assertionList = new ArrayList<>();

        if (assertionListNode == null || assertionListNode.isEmpty()) {
            throw new MipamsException("Assertion List cannot be empty");
        }

        Iterator<JsonNode> contentIterator = assertionListNode.elements();

        while (contentIterator.hasNext()) {
            ObjectNode assertionNode = (ObjectNode) contentIterator.next();

            Assertion assertion = deserializeAssertion(assertionNode);

            assertionList.add(assertion);
        }

        request.setAssertionList(assertionList);
    }

    private static List<RedactableAssertion> getRedactableAssertionList(JsonNode assertionListNode)
            throws MipamsException {
        List<RedactableAssertion> assertionList = new ArrayList<>();

        if (assertionListNode == null || assertionListNode.isEmpty()) {
            return assertionList;
        }

        Iterator<JsonNode> contentIterator = assertionListNode.elements();

        while (contentIterator.hasNext()) {
            ObjectNode assertionNode = (ObjectNode) contentIterator.next();

            Assertion assertion = deserializeAssertion(assertionNode);

            if (!(assertion instanceof ExifMetadataAssertion)) {
                throw new MipamsException(
                        "The following assertion is not a redactable one: " + assertionNode.toString());
            }

            assertionList.add((RedactableAssertion) assertion);
        }

        return assertionList;
    }

    private static List<String> populateStringList(FakeMediaRequest request, JsonNode assertionListNode) {
        List<String> result = new ArrayList<>();

        if (assertionListNode == null || assertionListNode.isEmpty()) {
            return result;
        }

        Iterator<JsonNode> contentIterator = assertionListNode.elements();

        while (contentIterator.hasNext()) {
            ObjectNode assertionNode = (ObjectNode) contentIterator.next();
            result.add(assertionNode.asText());
        }

        return result;
    }

    private static Assertion deserializeAssertion(JsonNode assertionNode) throws MipamsException {
        ObjectMapper mapper = new ObjectMapper();
        Assertion response;
        try {
            Class<? extends Assertion> assertionClass = getAssertionClassBasedOnNode(assertionNode);

            if (assertionClass == null) {
                throw new MipamsException("Could not deserialize input: " + assertionNode.toString());
            }

            // if (ExifMetadataAssertion.class.equals(assertionClass)) {
            // response = new
            // ExifMetadataAssertion(assertionNode.get("exifMetadata").asText());
            // } else {
            response = mapper.readValue(assertionNode.toString(), assertionClass);
            // }
        } catch (JsonProcessingException e) {
            throw new MipamsException(e.getMessage());
        }

        return response;
    }

    private static Class<? extends Assertion> getAssertionClassBasedOnNode(JsonNode jsonNode) {
        if (jsonNode.has("action")) {
            return ActionAssertion.class;
        } else if (jsonNode.has("digest")) {
            return BindingAssertion.class;
        } else if (jsonNode.has("relationship")) {
            return IngredientAssertion.class;
        } else if (jsonNode.has("fileName")) {
            return ThumbnailAssertion.class;
        } else if (jsonNode.has("exifMetadata")) {
            return ExifMetadataAssertion.class;
        } else {
            return null;
        }
    }

    public static String movefileToDirectory(String currentFileUrl, String outputFileUrl) throws MipamsException {

        String outputDir = outputFileUrl.substring(0, outputFileUrl.lastIndexOf("/"));
        File outputFile = new File(outputFileUrl);

        if (outputFile.exists()) {
            outputFileUrl = CoreUtils.getFullPath(outputDir, CoreUtils.randomStringGenerator());
            outputFile = new File(outputFileUrl);
        }

        File file = new File(currentFileUrl);

        if (!file.exists()) {
            throw new MipamsException("File " + currentFileUrl + " does not exist");
        }

        if (file.renameTo(outputFile)) {
            // if file copied successfully then delete the original file
            file.delete();
        } else {
            throw new MipamsException("Failed to move file: " + currentFileUrl);
        }

        return outputFileUrl;
    }

    public static List<String> getFileList(String dir) throws MipamsException {
        File examinedDir = new File(dir);

        List<String> result = new ArrayList<>();

        if (examinedDir.isDirectory()) {
            for (File childFile : examinedDir.listFiles()) {
                result.add(childFile.getName());
            }
        }

        return result;
    }
}