이 Cordova 플러그인은 raccoondev85님의 cordova-plugin-kakao-sdk를 수정하였습니다.

https://github.com/raccoondev85/cordova-plugin-kakao-sdk

안드로이드 카카오 링크 공유 부분만 사용 가능합니다.

순수 cordova 에서 작동하며 

추가로 카카오 관련 개발 계획 있을 시 추가 개발 들어갈 예정입니다.


Cordova plugin 설치

cordova plugin add https://github.com/sjwiq200/cordova-plugin-kakao.git —variable KAKAO_APP_KEY=YOUR_APP_KEY

카카오 링크 공유

let feedLink = {
  webURL: url
}

let feedSocial = {
  likeCount: 50
}

let feedButtons1 = {
  title: '웹에서 보기',
  link: {
    mobileWebURL: url
  }
}

let feedButtons2 = {
  title: 'button2',
  link: {
    iosExecutionParams: 'param1=value1&param2=value2',
    androidExecutionParams: 'param1=value1&param2=value2',
  }
}

let feedContent = {
  title: title,
  link: feedLink,
  imageURL: 'http://mud-kage.kakao.co.kr/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png'
};

let feedTemplate = {
  content: feedContent,
  social: feedSocial,
  buttons: [feedButtons1]
};

KakaoTalk.share(feedTemplate,
function (success) {
  console.dir('kakao share success');
},
function (error) {
  console.dir(error)
})
