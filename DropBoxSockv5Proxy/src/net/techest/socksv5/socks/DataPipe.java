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
 *  Created on : 2011-8-21, 下午1:53:28
 *  Author     : princehaku
 */

package net.techest.socksv5.socks;

import java.io.IOException;
import java.net.Socket;

import net.techest.socksv5.helper.Logger;


/**此种用于bind端口形式的数据转发  比如ftp
 * 会同时保持到客户机和目标服务器的连接
 * 在timeout或者两者其中之一断开后释放所有连接
 * @author princehaku
 *
 */
public class DataPipe implements IDataPipe {
	
	DataIncomeMoniter  dm1;
	DataIncomeMoniter  dm2;
	
	Socket sck1;
	
	Socket sck2;
	
	public boolean isKeepAlive(){
		boolean isa=(dm1.isAlive()&&dm2.isAlive());
		return isa;
	}
	
	/* (non-Javadoc)
	 * @see net.techest.socksv5.DataPipe#pipe(net.techest.socksv5.SocksSocket, net.techest.socksv5.SocksSocket)
	 */
	@Override
	public void pipe(SocksSocket ssck1, SocksSocket ssck2) {
		sck1 = ssck1.getSocket();
		sck2 = ssck2.getSocket();
		
		dm1 = new DataIncomeMoniter(ssck1,ssck2);
		dm2 = new DataIncomeMoniter(ssck2,ssck1);
		
		dm1.start();
		dm2.start();
	}

	public int getUploadTraffic()
	{
		return dm1.getTrafficSize();
	}
	public int getDownloadTraffic()
	{
		return dm2.getTrafficSize();
	}
	@Override
	public void release() throws SocksException {
		
		Logger.getInstance().log("资源已释放 At "+sck1.toString()+"   from DataPipe-release");
		Logger.getInstance().log("资源已释放 At "+sck2.toString()+"   from DataPipe-release");
		
		dm1.stopTransfer(true);
		dm2.stopTransfer(true);
		
		try {
			sck2.close();
			sck1.close();
		} catch (IOException e) {
			throw new SocksException("资源释放失败");
		}
		
	}
			
}
