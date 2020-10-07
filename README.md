# animatedgifs_app[움짤공작소] : 현재는 운영하지 않습니다.
 움짤공작소는 런칭한 어플로, 음성과 얼굴 합성을 통해 영화/드라마 속 명장면의 주인공이 되어보는 어플입니다.
 
 저는 해당 프로젝트에서 얼굴 인식 및 합성 기능을 담당하였습니다. 
 
 OpenCV와 dlib으로 facial landmarks을 추출하여 얼굴 인식 기능을 개발하였고, 
 
 인식된 회원의 프로필 사진으로 움짤을 만듭니다. 
 
 얼굴 합성은 Face swapping 기술을 이용하여 해당 어플에 맞게 개발하였습니다. 
 
 실제 런칭을 하면서 음성과 사진의 보관이 개인정보보호법과 관련되어 있어 해당 법안을 살펴보는 등의 경험을 통해 
 
 실제 서비스를 운영할 때는 개발 이외의 것도 공부할 것이 많다는 것을 느꼈습니다.



기능
----
1. 영화/드라마 명장면 리스트
2. 회원가입
3. 얼굴 인식
4. 음성 녹음
5. 얼굴 합성
6. 동영상 다운로드


나의 구현 기능
----------------
1. 얼굴 인식
2. 얼굴 합성


사용기술
--------
● 서버: GCP(Google Cloud Platform)

● 웹 서버: Nginx, uwsgi, flask

● 데이터베이스: Mysql

● 언어: Java, Python

● 라이브러리/API : OpenCV, dlib , FFmpeg


스크린샷
---------
<div>
<img src="https://user-images.githubusercontent.com/67361330/85956580-aec20900-b9c1-11ea-9803-61d06325df47.png" width="100%"></img>
<img src="https://user-images.githubusercontent.com/67361330/85956583-b4b7ea00-b9c1-11ea-8cad-77ad6e4069f6.png" width="100%"></img>
<img src="https://user-images.githubusercontent.com/67361330/85956584-b8e40780-b9c1-11ea-865f-ad06818f0649.png" width="100%"></img>
</div>


영상링크
--------
https://youtu.be/-84cWhlK658
 
 
라이센스
--------
MIT License

Copyright (c) 2020 seulgikim2019

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
