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
 *  Created on : 2011-8-21, 下午4:29:50
 *  Author     : princehaku
 */

package net.techest.socksv5.socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.techest.socksv5.helper.Logger;

/**这种类型的只发送和接受转发一次 之后直接关掉sockets
 * 
 * @author princehaku
 * @deprecated
 */
public class SendDataPipe implements IDataPipe {
	
	Socket sck1;
	
	Socket sck2;
	@Override
	public boolean isKeepAlive() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.techest.socksv5.DataPipe#pipe(net.techest.socksv5.SocksSocket,
	 * net.techest.socksv5.SocksSocket)
	 */
	@Override
	public void pipe(SocksSocket ssck1, SocksSocket ssck2) {
		sck1 = ssck1.getSocket();
	    sck2 = ssck2.getSocket();

	    Logger.getInstance().log("接收客户机消息" + sck1.toString());
		InputStream in1 = null;
		InputStream in2 = null;
		OutputStream out1 = null;
		OutputStream out2 = null;
		try {
			in1 = sck1.getInputStream();
			in2 = sck2.getInputStream();
			out1 = sck1.getOutputStream();
			out2 = sck2.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Logger.getInstance().log("发送给目标主机" + sck2.toString());
			int n = -1;
			while ((n = in1.read()) != -1) {
				sck1.setSoTimeout(3000);
				out2.write(n);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		try {
			Logger.getInstance().log("返回客户机" + sck2.toString());
			int n = -1;
			//Logger.getInstance().log("接收目标主机" + sck2.toString());
			n = -1;
			while ((n = in2.read()) != -1) {
				sck2.setSoTimeout(3000);
				out1.write(n);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		

	}

	@Override
	public void release() throws SocksException {
		
		Logger.getInstance().log("关闭sck");
		
		try {
			sck2.close();
			sck1.close();
		} catch (IOException e) {
			throw new SocksException("资源释放失败");
		}
		
	}

	@Override
	public int getUploadTraffic() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDownloadTraffic() {
		// TODO Auto-generated method stub
		return 0;
	}

}
