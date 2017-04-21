/*
 * Copyright 2014-2015 ieclipse.cn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mainaer.wjokhttp.url;

import android.text.TextUtils;

import com.mainaer.wjoklib.okhttp.IUrl;

import com.mainaer.wjoklib.okhttp.controller.OKHttpController.Method;


/**
 * 类/接口描述
 *
 * @author Jamling
 * @date 2015年11月16日
 */
public final class URLConst {
    private URLConst() {
    }

    // get使用url
    public static final String BASE = "http://api.1-blog.com/biz/bizserver/news/";
    // 下载使用url
    public static String DOWNLOADURL = "http://msoftdl.360.cn/mobilesafe/shouji360/360safesis/360StrongBox_1.0.9.1008"
        + ".apk";


    public interface Product{
        Url PRODUCTLIST = new Url("list.do").get();
    }

    public static class Url implements IUrl {
        protected int method;
        protected String url;
        protected String query;
        
        public Url(String url) {
            this.url = url;
        }
        
        public Url get() {
            this.method = Method.GET;
            return this;
        }
        
        public Url post() {
            this.method = Method.POST;
            return this;
        }
        
        public Url put() {
            this.method = Method.PUT;
            return this;
        }
        
        public Url delete() {
            this.method = Method.DELETE;
            return this;
        }
        
        public String getUrl() {
            return BASE + url + getQuery();
        }
        
        public int getMethod() {
            return method;
        }

        public String getQuery() {
            if (TextUtils.isEmpty(query)) {
                return "";
            }
            else if (url.indexOf("?") >= 0) {
                return "&" + query;
            }
            else {
                return "?" + query;
            }
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
    }
    
    public static class AbsoluteUrl extends Url {

        public AbsoluteUrl(String url) {
            super(url);
        }
        
        @Override
        public String getUrl() {
            return url + getQuery();
        }
    }
}
