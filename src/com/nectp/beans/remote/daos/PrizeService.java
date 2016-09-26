package com.nectp.beans.remote.daos;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Prize;

public interface PrizeService extends DataService<Prize> {

	public Prize selectPrizeByType(NEC prizeType);
	
}
