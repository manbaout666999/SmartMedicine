package com.example.smartmedicine.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import java.util.List;
import java.util.Map;
 
public interface QwenVLApi {

    @Multipart
    @POST("services/aigc/multimodal-generation/generation")
    Call<ResponseBody> recognizeMedicineByImage(
            @Part MultipartBody.Part image,
            @Part("model") RequestBody model,
            @Part("prompt") RequestBody prompt
    );

    @POST("services/aigc/multimodal-generation/generation")
    Call<ResponseBody> generate(@Body QwenVLRequest request);

    class QwenVLRequest {
        private String model;
        private List<Map<String, Object>> input;
        private Map<String, Object> parameters;

        public QwenVLRequest(String model, List<Map<String, Object>> input, Map<String, Object> parameters) {
            this.model = model;
            this.input = input;
            this.parameters = parameters;
        }

        public String getModel() {
            return model;
        }

        public List<Map<String, Object>> getInput() {
            return input;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }
    }
}
