package com.welkinlan.model;

import com.welkinlan.whackamole.R;
import com.welkinlan.whackamole.R.drawable;

public class Mole {

	private Integer mole; //mole image source
	private Integer butterfly; //butterfly image source
	private Integer hammer; //hammer image source

	public Mole() {
		mole = R.drawable.mole;
		butterfly = R.drawable.butterfly;
		hammer = R.drawable.hammer;
	}

	public Integer getMole() {

		return mole;
	}

	public Integer getButterfly() {

		return butterfly;
	}

	public Integer getHammer() {

		return hammer;
	}

}
