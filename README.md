<img src="http://alize.univ-avignon.fr/images/alize-logo.png" alt="The ALIZÉ logo" height="198" >

# ALIZÉ for Android — Tutorial

*This package is part of the ALIZÉ project: <http://alize.univ-avignon.fr>*



Welcome to ALIZÉ
----------------

ALIZÉ is a software platform for automatic speaker recognition. It can be used for carrying out research in this field, as well as for incorporating speaker recognition into applications.

<http://alize.univ-avignon.fr>


ALIZÉ for Android
-----------------

ALIZÉ for Android offers access to high-level APIs which allow to run a speaker detection system (for speaker verification/identification) on the Android platform. Through this API, a user can feed the system audio data and use it to train speaker models, and run speaker verification and identification tasks.

Official Android-ALIZÉ repository: <https://github.com/ALIZE-Speaker-Recognition/android-alize>.


Advanced tutorial
------------

This application serves as an advanced tutorial for the API, showing how to use the various methods (and what for) in concretes examples. For a more basic tutorial, you have this one: <https://github.com/ALIZE-Speaker-Recognition/Android-ALIZE-Tutorial>.

It is distributed as an application so that it is easy to play with the code and run it with your modifications. You can run it to understand how and why methods are used in concretes ways of use. 

This application use all the features specified in the class SimpleSpkDetSystem. It does speaker recognition demonstration with identification and verification methods. <br>
All the ALIZÉ features use in the code are commented and explained. The structure of this tutorial application is meant to be simple by having specific activities for each features. <br>
The recognition part use the Android native recorder which you can see the specifications in the class RecordActivity. The startRecording() method is separated in three Threads:
<ol>
    <li>A recording thread that use the Android native recorder and store the data collected in RAM.</li>
    <li>A thread use to read all the data synchronized by the previous thread and send them to the Alizé system.</li>
    <li>A more simple thread use to increase the timer of the record.</li>
</ol>
