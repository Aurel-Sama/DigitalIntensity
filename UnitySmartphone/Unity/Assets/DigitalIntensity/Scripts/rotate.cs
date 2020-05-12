using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class rotate : MonoBehaviour
{

    [SerializeField]
    Vector3 RotateByEularAngle = new Vector3(0f, 1f, 0f);

    [SerializeField]
    float rotationSpeed = 0.5f;

    [SerializeField]
    float blipScaleFactor = 2.0f;

    Vector3 originalscale;
    float multspeed=1;

    private void Update()
    {
        rotateeuler();
    }

    private void Start(){
        originalscale = transform.localScale;
    }
    public void hold(){
        multspeed = 0f;
    }
    public void unhold()
    {
        multspeed = 1f;
    }
    public void rotateeuler(){
        transform.eulerAngles = transform.eulerAngles + RotateByEularAngle * (rotationSpeed*multspeed);
    }
    public void BlipCoroutine()
    {
        StartCoroutine(BlipTargetCoroutine());
    }

    IEnumerator BlipTargetCoroutine()
    {
        transform.localScale = originalscale * blipScaleFactor;
        yield return new WaitForSeconds(0.1f);
    }
}
