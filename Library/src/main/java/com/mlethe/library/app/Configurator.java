package com.mlethe.library.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Interceptor;

/**
 * Created by Mlethe on 2018/6/4.
 */

public class Configurator {
  private static final HashMap<Object, Object> CONFIGS = new HashMap<>();
  private static final List<Interceptor> INTERCEPTORS = new ArrayList<>();

  //单例   java并发实战中推的方法
  private static class Holder {
    private static final Configurator INSTANCE = new Configurator();
  }

  public static Configurator getInstance() {
    return Holder.INSTANCE;
  }

  private Configurator() {
    CONFIGS.put(ConfigKeys.CONFIG_READY.name(), false);
    CONFIGS.put(ConfigKeys.LOG_ENABLE, false);
    CONFIGS.put(ConfigKeys.INTERCEPTORS, INTERCEPTORS);
  }

  /**
   * 自定义配置
   * @param key
   * @param value
   * @return
   */
  public final Configurator put(Object key, Object value) {
    CONFIGS.put(key, value);
    return this;
  }

  /**
   * 获取配置信息
   * @return
   */
  final HashMap<Object, Object> getConfigs() {
    return CONFIGS;
  }

  /**
   * 根据key获取value
   * @param key
   * @param <T>
   * @return
   */
  final <T> T getConfiguration(Object key) {
    checkConfiguration();
    final Object value = CONFIGS.get(key);
    if (value == null) {
      throw new NullPointerException(key.toString() + " IS NULL");
    }
    return (T) CONFIGS.get(key);
  }


  /**
   * 配置APIHOSTS
   * @param hosts
   * @return
   */
  public final Configurator withApiHost(String... hosts) {
    CONFIGS.put(ConfigKeys.API_HOSTS, hosts);
    return this;
  }

  /**
   * 添加拦截器
   * @param interceptor
   * @return
   */
  public final Configurator addInterceptor(Interceptor interceptor) {
    if (interceptor == null) throw new IllegalArgumentException("interceptor == null");
    INTERCEPTORS.add(interceptor);
    return this;
  }

  /**
   * 设置日志是否打印
   * @param enable
   * @return
   */
  public final Configurator setLogEnable(boolean enable) {
    return put(ConfigKeys.LOG_ENABLE, enable);
  }

  /**
   * 检查配置是否完成
   */
  private void checkConfiguration() {
    final boolean isReady = (boolean) CONFIGS.get(ConfigKeys.CONFIG_READY.name());
    if (!isReady) {
      throw new RuntimeException("Configuration is not ready,call configure()");
    }
  }

  /**
   * 配置完成
   */
  public final void configure() {
    CONFIGS.put(ConfigKeys.CONFIG_READY.name(), true);
  }


}














