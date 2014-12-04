package com.welkinlan.whackamole;

import com.welkinlan.whackamole.R;

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
