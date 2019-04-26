package com.clover.activity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.clover.adapter.FanInforLVAdapter;
import com.clover.adapter.ViewPageAdapter;
import com.clover.entity.Contact;
import com.clover.entity.FanParam;
import com.example.fancontroll.R;
import com.utils.StreamTool;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity implements OnClickListener,
		OnPageChangeListener {

	private final static String PATH = "http://10.0.2.2:8080/fan/";

	protected static final int NO_PARAM = 0;

	protected static final int SUCCESS = 1;

	protected static final int ERROR = 2;
	
	// 底部3个LinearLayout
	private LinearLayout ll_fan;
	private LinearLayout ll_contact;
	private LinearLayout ll_more;

	// 底部3个ImageView
	private ImageView iv_fan;
	private ImageView iv_contact;
	private ImageView iv_more;

	// 底部3个菜单标题
	private TextView tv_fan;
	private TextView tv_contact;
	private TextView tv_more;

	// 中间内容区域
	private ViewPager viewPager;

	// ViewPager适配器ContentAdapter
	private ViewPageAdapter adapter;

	private List<View> views;
	
	private List<FanParam> fanParamList = new ArrayList<FanParam>();
	private List<Contact> contactsList = new ArrayList<Contact>();
	
	/*
	 * 子页面控件
	 */
	//page_01控件
	private Spinner spinner;
	private ListView lv_fan_param;
	
	/*
	 * 子页面控件
	 */
	//page_02控件
	private ListView lv_contact;
	
	private Handler mHandler = new Handler(){

			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case SUCCESS:
					//显示风机各项参数
					/*
					 * 【扩展---MVC模式】
					 * M: 绑定到屏幕上的数据(Model)
					 * V: 视图，这里为ListView(View)
					 * C: 适配器，控制数据在平面上显示的方式，这里为ListViewAdapter(Controller)
					 */
					initFanInfor(msg.obj);
					lv_fan_param.setAdapter(new FanInforLVAdapter(HomeActivity.this,R.layout.param_item,fanParamList));
					break;
					
				case NO_PARAM:
					Toast.makeText(HomeActivity.this, "服务器尚无该风机的参数信息", 0).show();
					break;
					
				case ERROR:
					Toast.makeText(HomeActivity.this, "网络出错", 0).show();
					break;
				default:
					break;
				}
			};
			
		};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.page_main);

		// 初始化控件
		initView();

		// 初始化事件
		initEvent();
	}

	// 初始化控件
	private void initView() {

		this.ll_fan = (LinearLayout) findViewById(R.id.ll_fan);
		this.ll_contact = (LinearLayout) findViewById(R.id.ll_contact);
		this.ll_more = (LinearLayout) findViewById(R.id.ll_more);

		this.iv_fan = (ImageView) findViewById(R.id.iv_fan);
		this.iv_contact = (ImageView) findViewById(R.id.iv_contact);
		this.iv_more = (ImageView) findViewById(R.id.iv_more);

		this.tv_fan = (TextView) findViewById(R.id.tv_fan);
		this.tv_contact = (TextView) findViewById(R.id.tv_contact);
		this.tv_more = (TextView) findViewById(R.id.tv_more);

		// 中间内容区域ViewPager
		this.viewPager = (ViewPager) findViewById(R.id.vp_content);

		// 适配器
		View page_01 = View.inflate(HomeActivity.this, R.layout.page_01, null);
		View page_02 = View.inflate(HomeActivity.this, R.layout.page_02, null);
		View page_03 = View.inflate(HomeActivity.this, R.layout.page_03, null);

		views = new ArrayList<View>();
		views.add(page_01);
		views.add(page_02);
		views.add(page_03);

		this.adapter = new ViewPageAdapter(views);
		viewPager.setAdapter(adapter);
		
		//初始化每个子页面的控件
		spinner = (Spinner) page_01.findViewById(R.id.number_spinner);
		lv_fan_param = (ListView) page_01.findViewById(R.id.lv_fan_param);
		
		lv_contact = (ListView) page_02.findViewById(R.id.lv_contact);
		
	}

	// 初始化底部按钮事件
	private void initEvent() {

		// 设置按钮监听
		ll_fan.setOnClickListener(this);
		ll_contact.setOnClickListener(this);
		ll_more.setOnClickListener(this);

		// 设置ViewPager滑动监听
		viewPager.setOnPageChangeListener(this);
		
		//每个子页面的事件监听
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, 
                    int pos, long id) {
           
                String[] number = getResources().getStringArray(R.array.number);
                getParamFromServer(number[pos]);
                
                //清空List,为显示下一个风机参数留存空间
				fanParamList.clear();
                //Toast.makeText(HomeActivity.this, "你点击的是:"+number[pos], 2000).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
	}

	
	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		
		restartButton();
		
		//当前view被选择的时候,改变底部菜单图片，文字颜色
		switch (arg0) {
		case 0:
			iv_fan.setImageResource(R.drawable.fan_pressed);
			tv_fan.setTextColor(Color.parseColor("#629540"));
			break;

		case 1:
			iv_contact.setImageResource(R.drawable.person_pressed);
			tv_contact.setTextColor(Color.parseColor("#629540"));
			break;

		case 2:
			iv_more.setImageResource(R.drawable.more_pressed);
			tv_more.setTextColor(Color.parseColor("#629540"));
			break;

		default:
			break;
		}

	}

	@Override
	public void onClick(View arg0) {
		
		restartButton();
		
		switch (arg0.getId()) {
		case R.id.ll_fan:
			iv_fan.setImageResource(R.drawable.fan_pressed);
			tv_fan.setTextColor(Color.parseColor("#629540"));
			viewPager.setCurrentItem(0);
			break;

		case R.id.ll_contact:
			iv_contact.setImageResource(R.drawable.person_pressed);
			tv_contact.setTextColor(Color.parseColor("#629540"));
			viewPager.setCurrentItem(1);
			break;

		case R.id.ll_more:
			iv_more.setImageResource(R.drawable.more_pressed);
			tv_more.setTextColor(Color.parseColor("#629540"));
			viewPager.setCurrentItem(2);
			break;

		default:
			break;
		}
	}
	
	private void restartButton(){
		//ImageView设置为黑色
		iv_fan.setImageResource(R.drawable.fan);
		iv_contact.setImageResource(R.drawable.personal);
		iv_more.setImageResource(R.drawable.more);
		
		//TextView设置为黑色
		tv_fan.setTextColor(Color.parseColor("#000000"));
		tv_contact.setTextColor(Color.parseColor("#000000"));
		tv_more.setTextColor(Color.parseColor("#000000"));
		
	}
	
	private void getParamFromServer(final String fan_number){
	
    	//判断是否为空
    	if (TextUtils.isEmpty(fan_number) || TextUtils.isEmpty(fan_number)) {
			Toast.makeText(HomeActivity.this, "请选择风机编号", Toast.LENGTH_SHORT).show();
			return;
		}
    	
    	//向服务器获取风机参数
    	new Thread(){
    		public void run() {
    			
    			//连接网络
    	    	try {
    	    		//url编码传输
        			String wholeUrl = PATH + URLEncoder.encode(fan_number, "UTF-8");
        			
        			//String testUrl = "https://www.baidu.com/s?wd=%E4%BB%8A%E6%97%A5%E6%96%B0%E9%B2%9C%E4%BA%8B&tn=SE_Pclogo_6ysd4c7a&sa=ire_dl_gh_logo&rsv_dl=igh_logo_pc";
    				URL url = new URL(wholeUrl);
    				
    				//建立一个连接---Connection对象
    				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    				
    				//设置请求方式
    				conn.setRequestMethod("GET");
    				conn.setConnectTimeout(5000);
    				
    				//获得服务器返回的状态码，根据状态码判断访问是否成功
    				int code = conn.getResponseCode();
    				if (code == 200) { //成功处理请求
    					
    					InputStream in = conn.getInputStream();
    					
    					//将字符流转为字符串
    					String res = StreamTool.decodeStream(in);
    					
    					//System.out.println(data);
    					
    					//解析服务端回送的json格式的数据
    					JSONObject jsonObject = new JSONObject(res);
    					
    					//判断回送的数据是否为空
    					String result = jsonObject.getString("data");
    					
    					if (result == null) { //判断是否有数据返回
    						
    						//返回数据为空
    						Message msg = Message.obtain();
							msg.what = NO_PARAM;
							mHandler.sendMessage(msg);
							
						}else {
							
							//取出风机各项参数
							JSONObject dataObj = jsonObject.getJSONObject("data");
							
							//通知更新ui
							Message msg = Message.obtain();
							msg.obj = dataObj;
							msg.what = SUCCESS;
							mHandler.sendMessage(msg);
						}
    				}
    				
    			} catch (Exception e) {
    				
    				e.printStackTrace();
    				Message msg = Message.obtain();
					msg.what = ERROR;
					mHandler.sendMessage(msg);
    			}
    		};
    	}.start();
    }

    private void initFanInfor(Object fanParam){
    	JSONObject JsonObj = (JSONObject) fanParam; 
    	try {
			FanParam pressure = new FanParam("电压", JsonObj.getString("pressure"));
			fanParamList.add(pressure);
			
			FanParam electric = new FanParam("电流", JsonObj.getString("electric"));
			fanParamList.add(electric);
			
			FanParam activePower = new FanParam("有功功率", JsonObj.getString("activePower"));
			fanParamList.add(activePower);
			
			FanParam reactivePower = new FanParam("无功功率", JsonObj.getString("reactivePower"));
			fanParamList.add(reactivePower);
			
			FanParam windSpeed = new FanParam("风速", JsonObj.getString("windSpeed"));
			fanParamList.add(windSpeed);
			
			FanParam windAngle = new FanParam("风向", JsonObj.getString("windAngle"));
			fanParamList.add(windAngle);
			
			FanParam rotationSpeed = new FanParam("转速", JsonObj.getString("rotationSpeed"));
			fanParamList.add(rotationSpeed);
			
			FanParam temperature = new FanParam("温度", JsonObj.getString("temperature"));
			fanParamList.add(temperature);
			
			FanParam cableAngle = new FanParam("电缆扭转角度", JsonObj.getString("cableAngle"));
			fanParamList.add(cableAngle);
			
			FanParam dynamoTem = new FanParam("发电机温度", JsonObj.getString("dynamoTem"));
			fanParamList.add(dynamoTem);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
}
