package dv.dimonvideo.dvadmin.util;

import dv.dimonvideo.dvadmin.Config;
import dv.dimonvideo.dvadmin.model.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET(Config.COUNT_URL)
    Call<ApiResponse> getCounts();
}
