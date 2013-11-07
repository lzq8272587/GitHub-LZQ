/*  Copyright 2010 princehaku
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on : 2011-8-21, 下午6:29:49
 *  Author     : princehaku
 */

package net.techest.socksv5.socks;

import java.io.IOException;

import net.techest.socksv5.helper.Logger;

/**数据转发监控线程
 * 会一直监控从ssck1读取的数据
 * 如果有则立即写入到ssck2里面
 * 就算读取超时也会一直等待
 * 
 * @author princehaku
 *
 */
public class DataIncomeMoniter extends Thread{
	
	private int cacheSize = 102400;

	SocksSocket ssck1;
	
	SocksSocket ssck2;

	private String closeMsg;
	
	private int traffic=0;
	
	public DataIncomeMoniter(SocksSocket ssck1, SocksSocket ssck2) {
		super();
		this.ssck1 = ssck1;
		this.ssck2 = ssck2;
		this.ssck1.setReadTimeOut(70000);
		this.ssck2.setReadTimeOut(70000);
	}

	public int getCacheSize() {
		return cacheSize;
	}

	
	public int getTrafficSize()
	{
		return traffic;
	}
	@Override
	public void run(){
		int n=-2;
//		Timer t=new Timer();
//		t.setInterval(5000);
		byte[] cache=new byte[this.cacheSize];
		//这个线程类的作用是，从sck1中读取数据，然后原封不动的放到sck2中
		while(true){
			
			try {
				n=ssck1.getSocket().getInputStream().read(cache);
				
				if(n==-1){
					Logger.getInstance().log("ssk1流被关闭 退出");
					break;
				}
				//n = ssck1.pull();
			} catch (IOException e) {
				n=-2;
				//如果是读取超时则继续等待
				if("Read timed out".equals(e.getMessage())){
					//Logger.getInstance().log(this.name+"读取超时 继续等待");
					continue;
				}else{
					//Logger.getInstance().log(this.name+"流读取错误  退出"+e.getMessage());
					//e.printStackTrace();
					break;
				}
			}
			
			if(!ssck2.getSocket().isConnected()||ssck2.getSocket().isOutputShutdown()||ssck2.getSocket().isClosed()){
				//Logger.getInstance().log("ssk2流被关闭 退出");
				break;
			}
			
			try {
				if(n>0){
					ssck2.getSocket().getOutputStream().write(cache,0,n);
					//如果读取到了数据，则对应的记录流量信息
					traffic+=n;
				}
				//ssck2.push(n);
			} catch (IOException e) {
				//Logger.getInstance().log(this.name+"写入失败");
				//如果是写错误就退出
				break;
			}
		}
		
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	public String getCloseMsg(){
		return this.closeMsg;
	}

	public void stopTransfer(boolean b) {
		//未定义
	}

}
