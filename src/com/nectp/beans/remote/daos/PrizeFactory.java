package com.nectp.beans.remote.daos;

import com.nectp.jpa.constants.NEC;
import com.nectp.jpa.entities.Prize;

public interface PrizeFactory extends PrizeService {

	public Prize createPrize(NEC prizeType);
	
}
