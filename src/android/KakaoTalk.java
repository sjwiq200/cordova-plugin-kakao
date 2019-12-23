package com.sjwiq200.plugin.kakao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.CommerceTemplate;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.ListTemplate;
import com.kakao.message.template.LocationTemplate;
import com.kakao.message.template.SocialObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.KakaoParameterException;
import com.kakao.util.exception.KakaoException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class KakaoTalk extends CordovaPlugin {

	private static final String LOG_TAG = "KakaoTalk";
	private static volatile Activity currentActivity;
	private KakaoLinkResponseCallback kakaoLinkResponseCallback;

	/**
	 * Initialize cordova plugin kakaotalk
	 * @param cordova
	 * @param webView
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView webView)
	{
		Log.v(LOG_TAG, "kakao : initialize");
		super.initialize(cordova, webView);
		currentActivity = this.cordova.getActivity();
		KakaoSDK.init(new KakaoSDKAdapter());
	}

	/**
	 * Execute plugin
	 * @param action
	 * @param options
	 * @param callbackContext
	 */
	public boolean execute(final String action, JSONArray options, final CallbackContext callbackContext) throws JSONException
	{
		Log.v(LOG_TAG, "kakao : execute " + action);
		cordova.setActivityResultCallback(this);
//		callback = new SessionCallback(callbackContext);
		removeSessionCallback();
//		Session.getCurrentSession().addCallback(callback);

		if (action.equals("share")) {

			kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
			this.share(options, callbackContext);

			return true;
		}
		return false;
	}

	private void removeSessionCallback() {
		Session.getCurrentSession().clearCallbacks();
	}

	private void share(JSONArray options, final CallbackContext callbackContext){
		try {
			final JSONObject object = options.getJSONObject(0);
			if (object == null) {
				callbackContext.error("feed template is null.");
				return;
			}
			if (!object.has("content")) {
				callbackContext.error("content is null.");
				return;
			}

			ContentObject contentObject = getContentObject(object.getJSONObject("content"));
			if (contentObject == null) {
				callbackContext.error("Either Content or Content.title/link/imageURL is null.");
				return;
			}

			FeedTemplate.Builder feedTemplateBuilder = new FeedTemplate.Builder(contentObject);

			if (object.has("social")) {
				SocialObject socialObject = getSocialObject(object.getJSONObject("social"));
				if (socialObject != null) {
					feedTemplateBuilder.setSocial(socialObject);
				}
			}

			addButtonsArray(object, feedTemplateBuilder);

			KakaoLinkService.getInstance().sendDefault(currentActivity, feedTemplateBuilder.build(),
					new KakaoLinkResponseCallback(callbackContext));

		} catch (Exception e) {
			e.printStackTrace();
			callbackContext.error(e.getMessage());
		}
	}

	/**
	 * On activity result
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		Log.v(LOG_TAG, "kakao : onActivityResult : " + requestCode + ", code: " + resultCode);
		if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, intent)) {
			return;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}


	private ContentObject getContentObject(JSONObject object) {
		if (object == null) {
			return null;
		}
		ContentObject.Builder contentObjectBuilder;
		try {
			LinkObject linkObject = getLinkObject(object.getJSONObject("link"));
			if (!object.has("title") || linkObject == null || !object.has("imageURL")) {
				return null;
			}
			contentObjectBuilder = new ContentObject.Builder(object.getString("title"), object.getString("imageURL"),
					linkObject);

			if (object.has("desc")) {
				contentObjectBuilder.setDescrption(object.getString("desc"));
			}
			if (object.has("imageWidth")) {
				contentObjectBuilder.setImageWidth(object.getInt("imageWidth"));
			}
			if (object.has("imageHeight")) {
				contentObjectBuilder.setImageHeight(object.getInt("imageHeight"));
			}
		} catch (Exception e) {
			return null;
		}

		return contentObjectBuilder.build();
	}

	private LinkObject getLinkObject(JSONObject object) {
		if (object == null) {
			return null;
		}
		LinkObject.Builder linkObjectBuilder = new LinkObject.Builder();
		try {
			if (object.has("webURL")) {
				linkObjectBuilder.setWebUrl(object.getString("webURL"));
			}
			if (object.has("mobileWebURL")) {
				linkObjectBuilder.setMobileWebUrl(object.getString("mobileWebURL"));
			}
			if (object.has("androidExecutionParams")) {
				linkObjectBuilder.setAndroidExecutionParams(object.getString("androidExecutionParams"));
			}
			if (object.has("iosExecutionParams")) {
				linkObjectBuilder.setIosExecutionParams(object.getString("iosExecutionParams"));
			}
		} catch (Exception e) {
			return null;
		}
		return linkObjectBuilder.build();
	}

	private SocialObject getSocialObject(JSONObject object) {
		if (object == null) {
			return null;
		}
		SocialObject.Builder socialObjectBuilder = new SocialObject.Builder();
		try {
			if (object.has("likeCount")) {
				socialObjectBuilder.setLikeCount(object.getInt("likeCount"));
			}
			if (object.has("commentCount")) {
				socialObjectBuilder.setCommentCount(object.getInt("commentCount"));
			}
			if (object.has("sharedCount")) {
				socialObjectBuilder.setSharedCount(object.getInt("sharedCount"));
			}
			if (object.has("viewCount")) {
				socialObjectBuilder.setViewCount(object.getInt("viewCount"));
			}
			if (object.has("subscriberCount")) {
				socialObjectBuilder.setSubscriberCount(object.getInt("subscriberCount"));
			}
		} catch (Exception e) {
			return null;
		}
		return socialObjectBuilder.build();
	}

	private ButtonObject getButtonObject(JSONObject object) {
		if (object == null) {
			return null;
		}
		ButtonObject buttonObject;
		try {
			LinkObject linkObject = getLinkObject(object.getJSONObject("link"));
			if (!object.has("title") || linkObject == null) {
				return null;
			}
			buttonObject = new ButtonObject(object.getString("title"), linkObject);
		} catch (Exception e) {
			return null;
		}

		return buttonObject;
	}

	private void addButtonsArray(JSONObject object, Object template) {
		if (object == null) {
			return;
		}
		try {
			if (object.has("buttons")) {
				JSONArray buttons = new JSONArray(object.getString("buttons"));
				if (buttons.length() < 1) {
					return;
				}
				for (int i = 0; i < buttons.length(); i++) {
					ButtonObject buttonObject = getButtonObject(buttons.getJSONObject(i));
					if (buttonObject == null) {
						continue;
					}
					if (template instanceof FeedTemplate.Builder) {
						((FeedTemplate.Builder) template).addButton(buttonObject);
					} else if (template instanceof ListTemplate.Builder) {
						((ListTemplate.Builder) template).addButton(buttonObject);
					} else if (template instanceof LocationTemplate.Builder) {
						((LocationTemplate.Builder) template).addButton(buttonObject);
					} else if (template instanceof CommerceTemplate.Builder) {
						((CommerceTemplate.Builder) template).addButton(buttonObject);
					} else if (template instanceof TextTemplate.Builder) {
						((TextTemplate.Builder) template).addButton(buttonObject);
					}
				}
			}
		} catch (Exception e) {
			return;
		}
	}

	private class KakaoLinkResponseCallback extends ResponseCallback<KakaoLinkResponse> {

		private CallbackContext callbackContext;

		public KakaoLinkResponseCallback(final CallbackContext callbackContext) {
			this.callbackContext = callbackContext;
		}

		@Override
		public void onFailure(ErrorResult errorResult) {
			callbackContext.error("kakao : SessionCallback.onSessionOpened.requestMe.onFailure - " + errorResult);
		}

		@Override
		public void onSuccess (KakaoLinkResponse result) {
			callbackContext.success(200);
		}
	}



	/**
	 * Return current activity
	 */
	public static Activity getCurrentActivity()
	{
		return currentActivity;
	}

	/**
	 * Set current activity
	 */
	public static void setCurrentActivity(Activity currentActivity)
	{
		currentActivity = currentActivity;
	}

	/**
	 * Class KakaoSDKAdapter
	 */
	private static class KakaoSDKAdapter extends KakaoAdapter {

		@Override
		public ISessionConfig getSessionConfig() {
			return new ISessionConfig() {
				@Override
				public AuthType[] getAuthTypes() {
					return new AuthType[] {AuthType.KAKAO_TALK};
				}

				@Override
				public boolean isUsingWebviewTimer() {
					return false;
				}

				@Override
				public ApprovalType getApprovalType() {
					return ApprovalType.INDIVIDUAL;
				}

				@Override
				public boolean isSaveFormData() {
					return true;
				}

				@Override
				public boolean isSecureMode() {
					return false;
				}
			};
		}

		@Override
		public IApplicationConfig getApplicationConfig() {
			return new IApplicationConfig() {

				@Override
				public Context getApplicationContext() {
					return KakaoTalk.getCurrentActivity().getApplicationContext();
				}
			};
		}
	}

}
