package com.mlethe.library.net;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Mlethe on 2019/6/2.
 */
public class UploadUtil {

    /**
     * 创建表单文件请求对象
     * @param key 参数名
     * @param file 文件
     * @return
     */
    public static final MultipartBody.Part createMultipartBodyPartOfForm(String key, File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData(key, file.getName(), requestBody);
    }

    /**
     * 创建表单同名多文件请求对象
     * @param key
     * @param files
     * @return
     */
    public static final List<MultipartBody.Part> createMultipartBodyPartsOfForm(String key, File... files){
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (File file : files) {
            parts.add(createMultipartBodyPartOfForm(key, file));
        }
        return parts;
    }

    /**
     * 创建文字请求对象
     * @param value
     * @return
     */
    public static final RequestBody createRequestBodyOfText(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    /**
     * 创建图片请求对象
     * @param value
     * @return
     */
    public static final RequestBody createRequestBodyOfImage(String value) {
        return RequestBody.create(MediaType.parse("image/*"), value);
    }

    /**
     * 创建表单请求对象
     * @param value
     * @return
     */
    public static final RequestBody createRequestBodyOfForm(String value) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), value);
    }
}
