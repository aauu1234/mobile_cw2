package org.tensorflow.lite.examples.classification;

import android.graphics.Bitmap;

import java.io.Serializable;

public class PlantModels implements Serializable {
    String plantname;
    String status;
    String info;
    String drugEffect;
    String curing;
    Bitmap PlantImage;
    String enName;

    public PlantModels(){

    }

    public PlantModels(String plantname,String status){
        this.plantname=plantname;
        this.status=status;


    }
    public PlantModels(String plantname,String enName,String status,String info,String drugEffect,String curing,Bitmap PlantImage){
        this.plantname=plantname;
        this.status=status;
        this.enName=enName;
        this.curing=curing;
        this.drugEffect=drugEffect;
        this.info=info;
        this.PlantImage=PlantImage;


    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlantname() {
        return plantname;
    }

    public void setPlantname(String plantname) {
        this.plantname = plantname;
    }

    public String getInfo(){
        return info;
    }
    public void setInfo(String info){
        this.info=info;
    }
    public String getDrugEffect(){
        return drugEffect;
    }
    public void setDrugEffect(String drugEffect){
        this.drugEffect=drugEffect;
    }
    public String getCuring(){
        return curing;
    }
    public void setCuring(String curing){
        this.curing=curing;
    }
    public String getEnName(){
        return enName;
    }
    public void setEnName(String enName){
        this.enName=enName;
    }
    public Bitmap getPlantImage(){
        return PlantImage;
    }
    public void setPlantImage(Bitmap plantImage){
        this.PlantImage=PlantImage;
    }

}
