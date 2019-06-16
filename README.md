# TwimgSpeedProxy
안드로이드 트위터 공앱의 이미지 속도를 향상시키는 트위터 이미지 고속 프록시입니다.

VPN을 사용하지 않고 공앱의 프록시 기능을 사용하므로, 삼성페이나 기타 VPN 사용 앱들과 충돌이 나지 않습니다.

트위터 이미지 고속 프록시가 Play Store에 출시되었습니다!
* [Play Store에서 다운받기](https://play.google.com/store/apps/details?id=com.sokcuri.twimgspeedproxy)

APK를 수동으로 다운받으려는 분들은 [릴리즈 페이지](https://github.com/sokcuri/TwimgSpeedProxy/releases)를 방문해주세요

**앱 설치 후 서비스를 시작해야 하고, 추가적으로 트위터 공식 앱의 프록시 설정이 필요합니다. 설명서를 꼭 참고하세요**

* [트위터 이미지 고속 프록시 설명서](https://docs.google.com/document/d/e/2PACX-1vSJr_ajbtPPDHl_9YXjl_-tr8eBprA0MJwN3PT8fU4-dOVpybbxOUVhDo0sOCMxiL86P1QhFDGp_M6e/pub)

## 관련 프로젝트
* [트위터 이미지 속도 패치](https://github.com/sokcuri/TwimgSpeedPatch)
* [트위터 이미지 서버 노드 퍼포먼스 측정용 앱](https://github.com/sokcuri/twimg-mon)

## 작동원리
내부적으로 [LittleProxy](https://github.com/ganskef/LittleProxy)를 사용하며, 트위터 이미지 서버(pbs.twimg.com)에 요청시 상태가 좋은 서버 노드로 리퀘스트합니다.

등록된 ip 중 하나로 리퀘스트를 바꿔 안드로이드에서도 빠른 속도로 이미지를 받아옵니다. [트위터 이미지 속도 패치](https://github.com/sokcuri/TwimgSpeedPatch) 리포지토리의 [server_ip](https://github.com/sokcuri/TwimgSpeedPatch/blob/master/data/server_ip.json) json 파일을 참고하세요.

## 참고사항
* HTTP Proxy는 HTTPS로 터널링되기 때문에 제작자도 앱도 트윗 내용이나 오가는 정보를 볼수도 알수도 없습니다.
* 트위터 고속 프록시 서비스를 실행시키고, 트위터 공앱의 프록시 설정이 필요합니다. 자세한 내용은 설명서를 참고하세요.
* 이 프로그램은 개인정보를 수집하고 있지 않습니다.
* 구글 플레이 스토어 업로드 대기중입니다.
