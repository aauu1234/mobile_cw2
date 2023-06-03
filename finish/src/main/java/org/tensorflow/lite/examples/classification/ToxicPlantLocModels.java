package org.tensorflow.lite.examples.classification;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ToxicPlantLocModels implements Serializable {
    int plantLocaSeq;
    int plantSeq;
    double locaLatitude;
    double locaLongitude;
    String locaEnName;
    String locaCnName;
    String plantEnName;
    String plantCnName;
    Bitmap PlantImage;
    String status;
    public ToxicPlantLocModels(){

    }

    public ToxicPlantLocModels(int plantLocaSeq, int plantSeq, double locaLatitude, double locaLongitude, String locaEnName, String locaCnName, String plantEnName, String plantCnName, Bitmap PlantImage,String status){
    this.plantLocaSeq=plantLocaSeq;
    this.plantSeq=plantSeq;
    this.locaLatitude=locaLatitude;
    this.locaLongitude=locaLongitude;
    this.locaEnName=locaEnName;
    this.locaCnName=locaCnName;
    this.plantEnName=plantEnName;
    this.plantCnName=plantCnName;
    this.PlantImage=PlantImage;
    this.status=status;


    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPlantLocaSeq(int plantLocaSeq){
        this.plantLocaSeq=plantLocaSeq;
    }

    public void setPlantSeq(int plantSeq){
        this.plantSeq=plantSeq;
    }
    public void setLocaLatitude(double locaLatitude){
        this.locaLatitude=locaLatitude;
    }
    public void setLocaLongitude(double locaLongitude){
        this.locaLongitude=locaLongitude;
    }
    public void setLocaEnName(String locaEnName){
        this.locaEnName=locaEnName;
    }

    public void setLocaCnName(String locaCnName){
        this.locaCnName=locaCnName;
    }

    public void setPlantEnName(String plantEnName){
        this.plantEnName=plantEnName;
    }
    public void setPlantCnName(String plantCnName){
        this.plantCnName=plantCnName;
    }
    public void setPlantImage(Bitmap plantImage){this.PlantImage=plantImage;}

    public int getPlantLocaSeq(){
        return this.plantLocaSeq;
    }

    public int getPlantSeq(){
        return  this.plantSeq;
    }
    public double getLocaLatitude(){
        return   this.locaLatitude;
    }
    public double getLocaLongitude(){
        return   this.locaLongitude;
    }
    public String getLocaEnName(){
        return  this.locaEnName;
    }

    public String getLocaCnName(){
        return  this.locaCnName;
    }

    public String getPlantEnName(){
        return this.plantEnName;
    }
    public String getPlantCnName(){
        return this.plantCnName;
    }
    public Bitmap getPlantImage(){return this.PlantImage;}

}
